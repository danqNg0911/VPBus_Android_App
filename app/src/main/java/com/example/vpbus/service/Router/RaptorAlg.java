package com.example.vpbus.service.Router;

import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.RaptorParentInfo;
import com.example.vpbus.model.Trip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RaptorAlg {

    private int parseTime(String timeStr) {
        String[] parts = timeStr.split(":");
        return Integer.parseInt(parts[0]) * 3600
                + Integer.parseInt(parts[1]) * 60
                + Integer.parseInt(parts[2]);
    }

    private String fmtTime(int sec) {
        int normalized = ((sec % 86400) + 86400) % 86400;
        int h = normalized / 3600;
        int m = (normalized % 3600) / 60;
        int s = normalized % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public List<List<Map<String, Object>>> runRaptor(
            TransitData data,
            List<String> fromStops,
            List<String> toStops,
            Calendar startTime,
            int maxTransfers,
            Map<String, List<NearestStopFinder.StopDistance>> nearbyStopMap,
            double walkingThreshold
    ) {
        Map<String, String> tripToRoute = new HashMap<>();
        for (Trip trip : data.trips) {
            tripToRoute.put(trip.getTrip_id(), trip.getRoute_id());
        }
        return runRaptorCore(
                data.stopTimesByTrip,
                tripToRoute,
                fromStops,
                toStops,
                startTime,
                maxTransfers,
                nearbyStopMap,
                walkingThreshold
        );
    }

    public List<List<Map<String, Object>>> runRaptor(
            List<Trip> trips,
            List<BusStopTimes> stopTimes,
            List<String> fromStops,
            List<String> toStops,
            Calendar startTime,
            int maxTransfers,
            Map<String, List<NearestStopFinder.StopDistance>> nearbyStopMap,
            double walkingThreshold
    ) {
        Map<String, List<BusStopTimes>> stopTimesByTrip = new HashMap<>();
        for (BusStopTimes st : stopTimes) {
            stopTimesByTrip.computeIfAbsent(st.getTrip_id(), k -> new ArrayList<>()).add(st);
        }
        for (List<BusStopTimes> list : stopTimesByTrip.values()) {
            list.sort(Comparator.comparingInt(BusStopTimes::getStop_sequence));
        }

        Map<String, String> tripToRoute = new HashMap<>();
        for (Trip trip : trips) {
            tripToRoute.put(trip.getTrip_id(), trip.getRoute_id());
        }

        return runRaptorCore(
                stopTimesByTrip,
                tripToRoute,
                fromStops,
                toStops,
                startTime,
                maxTransfers,
                nearbyStopMap,
                walkingThreshold
        );
    }

    private List<List<Map<String, Object>>> runRaptorCore(
            Map<String, List<BusStopTimes>> stopTimesByTrip,
            Map<String, String> tripToRoute,
            List<String> fromStops,
            List<String> toStops,
            Calendar startTime,
            int maxTransfers,
            Map<String, List<NearestStopFinder.StopDistance>> nearbyStopMap,
            double walkingThreshold
    ) {
        int startSeconds = startTime.get(Calendar.HOUR_OF_DAY) * 3600
                + startTime.get(Calendar.MINUTE) * 60
                + startTime.get(Calendar.SECOND);

        List<Map<String, Integer>> earliest = new ArrayList<>();
        List<Map<String, RaptorParentInfo>> parent = new ArrayList<>();
        List<Set<String>> marked = new ArrayList<>();

        for (int i = 0; i <= maxTransfers; i++) {
            earliest.add(new HashMap<>());
            parent.add(new HashMap<>());
            marked.add(new HashSet<>());
        }

        for (String stopId : fromStops) {
            earliest.get(0).put(stopId, startSeconds);
            marked.get(0).add(stopId);
        }

        for (int round = 1; round <= maxTransfers; round++) {
            Set<String> markedRound = new HashSet<>();

            for (Map.Entry<String, List<BusStopTimes>> entry : stopTimesByTrip.entrySet()) {
                String tripId = entry.getKey();
                List<BusStopTimes> stList = entry.getValue();
                String routeId = tripToRoute.get(tripId);

                Integer boardingIdx = null;
                String boardingStopId = null;
                Integer boardingDepartSec = null;

                for (int i = 0; i < stList.size(); i++) {
                    BusStopTimes st = stList.get(i);
                    String stopId = st.getStop_id();
                    int departSec = parseTime(st.getDeparture_time());

                    if (boardingIdx == null && marked.get(round - 1).contains(stopId)) {
                        int prevEarliest = earliest.get(round - 1).getOrDefault(stopId, Integer.MAX_VALUE);
                        if (departSec >= prevEarliest) {
                            boardingIdx = i;
                            boardingStopId = stopId;
                            boardingDepartSec = departSec;
                        }
                    } else if (boardingIdx != null && i > boardingIdx) {
                        int arrSec = parseTime(st.getArrival_time());
                        int currentBest = earliest.get(round).getOrDefault(stopId, Integer.MAX_VALUE);

                        if (arrSec < currentBest) {
                            earliest.get(round).put(stopId, arrSec);
                            markedRound.add(stopId);

                            RaptorParentInfo info = new RaptorParentInfo();
                            info.mode = "bus";
                            info.prevRound = round - 1;
                            info.prevStop = boardingStopId;
                            info.tripId = tripId;
                            info.routeId = routeId;
                            info.fromStop = boardingStopId;
                            info.toStop = stopId;
                            info.departTimeSec = boardingDepartSec;
                            info.arriveTimeSec = arrSec;
                            parent.get(round).put(stopId, info);
                        }
                    }
                }
            }

            if (walkingThreshold > 0) {
                List<String> newlyMarked = new ArrayList<>(markedRound);
                for (String stop : newlyMarked) {
                    int arriveSecHere = earliest.get(round).get(stop);
                    List<NearestStopFinder.StopDistance> nearby = nearbyStopMap.getOrDefault(stop, new ArrayList<>());

                    for (NearestStopFinder.StopDistance sd : nearby) {
                        if (sd.distance > walkingThreshold) continue;

                        int walkTimeSec = (int) ((sd.distance / 80.0) * 60.0);
                        int newArrival = arriveSecHere + walkTimeSec;
                        int currentBest = earliest.get(round).getOrDefault(sd.stopId, Integer.MAX_VALUE);

                        if (newArrival < currentBest) {
                            earliest.get(round).put(sd.stopId, newArrival);
                            markedRound.add(sd.stopId);

                            RaptorParentInfo info = new RaptorParentInfo();
                            info.mode = "walk";
                            info.prevRound = round;
                            info.prevStop = stop;
                            info.fromStop = stop;
                            info.toStop = sd.stopId;
                            info.distM = sd.distance;
                            info.arriveTimeSec = newArrival;
                            parent.get(round).put(sd.stopId, info);
                        }
                    }
                }
            }

            if (markedRound.isEmpty()) break;
            marked.get(round).addAll(markedRound);
        }

        return reconstructSolutions(toStops, earliest, parent, maxTransfers);
    }

    private List<List<Map<String, Object>>> reconstructSolutions(
            List<String> toStops,
            List<Map<String, Integer>> earliest,
            List<Map<String, RaptorParentInfo>> parent,
            int maxTransfers
    ) {
        List<List<Map<String, Object>>> solutions = new ArrayList<>();
        for (String target : toStops) {
            int bestRound = -1;
            int bestTime = Integer.MAX_VALUE;

            for (int round = 0; round <= maxTransfers; round++) {
                int t = earliest.get(round).getOrDefault(target, Integer.MAX_VALUE);
                if (t < bestTime) {
                    bestTime = t;
                    bestRound = round;
                }
            }

            if (bestRound == -1 || bestTime == Integer.MAX_VALUE) continue;

            List<Map<String, Object>> legs = new ArrayList<>();
            String currStop = target;
            int currRound = bestRound;

            while (currRound >= 0 && currStop != null) {
                RaptorParentInfo info = parent.get(currRound).get(currStop);
                if (info == null) break;

                if ("bus".equals(info.mode)) {
                    Map<String, Object> leg = new HashMap<>();
                    leg.put("type", "bus");
                    leg.put("route_id", info.routeId);
                    leg.put("trip_id", info.tripId);
                    leg.put("from_stop", info.fromStop);
                    leg.put("to_stop", info.toStop);
                    leg.put("depart_time", fmtTime(info.departTimeSec));
                    leg.put("arrive_time", fmtTime(info.arriveTimeSec));
                    legs.add(0, leg);
                    currStop = info.prevStop;
                    currRound = info.prevRound;
                } else {
                    currStop = info.prevStop;
                    currRound = info.prevRound;
                }
            }

            if (!legs.isEmpty()) solutions.add(legs);
        }
        return solutions;
    }
}
