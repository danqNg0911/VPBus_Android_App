package com.example.vpbus.service.Router;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Journey;
import com.example.vpbus.model.JourneyLeg;

import java.util.*;

public class TransitPlanner {

    private final AppDatabase db;
    private final RaptorAlg raptorRouter;

    public TransitPlanner(AppDatabase db) {
        this.db = db;
        this.raptorRouter = new RaptorAlg();
    }

    // ------------------ Helpers: geo/time ------------------

    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        return NearestStopFinder.bearing(lat1, lon1, lat2, lon2);
    }

    private double calculateAngleDiff(double a, double b) {
        return NearestStopFinder.angleDifference(a, b);
    }

    // ------------------ Ranking logic ------------------

    private List<String> rankStartStopsDirectional(
            List<BusStop> nearbyStartStops,
            Map<String, BusStop> stopsById,
            Map<String, List<BusStopTimes>> stbt,
            double destLat, double destLon,
            int startSeconds
    ) {
        if (nearbyStartStops.isEmpty()) return new ArrayList<>();

        // Logic "preferred_side" dựa trên ký tự đầu của stopId (giống Python)
        String preferredSide = null;
        String firstId = nearbyStartStops.get(0).getStop_id();
        if (firstId != null && !firstId.isEmpty() && Character.isDigit(firstId.charAt(0))) {
            preferredSide = String.valueOf(firstId.charAt(0));
        }

        List<ScoredStop> scored = new ArrayList<>();
        for (BusStop s : nearbyStartStops) {
            String sid = s.getStop_id();
            double baseDist = s.distance;
            double wantBearing = calculateBearing(s.getStop_lat(), s.getStop_lon(), destLat, destLon);

            List<Double> departBearings = getDepartingBearings(sid, stbt, stopsById, startSeconds);

            if (departBearings.isEmpty()) {
                scored.add(new ScoredStop(sid, baseDist, 600, 999.0));
                continue;
            }

            double bestDiff = Double.MAX_VALUE;
            for (double b : departBearings) {
                bestDiff = Math.min(bestDiff, calculateAngleDiff(b, wantBearing));
            }

            int penSec = 0;
            if (bestDiff > 110.0) continue; // Hard threshold
            else if (bestDiff > 70.0) penSec += 120; // Soft penalty

            if (preferredSide != null && !sid.startsWith(preferredSide)) {
                penSec += 60; // Penalty khác lề đường
            }

            scored.add(new ScoredStop(sid, baseDist, penSec, bestDiff));
        }

        scored.sort(Comparator.comparingDouble(item -> item.distance + item.penaltySec * 1.4));

        List<String> result = new ArrayList<>();
        for (ScoredStop ss : scored) result.add(ss.stopId);
        return result;
    }

    private List<Double> getDepartingBearings(String stopId, Map<String, List<BusStopTimes>> stbt,
                                              Map<String, BusStop> stopsById, int startSeconds) {
        List<Double> bearings = new ArrayList<>();
        for (List<BusStopTimes> seq : stbt.values()) {
            for (int i = 0; i < seq.size() - 1; i++) {
                BusStopTimes st = seq.get(i);
                if (st.getStop_id().equals(stopId)) {
                    // Chuyển arrivalTime sang giây để so sánh (giả định đã có helper parse)
                    if (parseHms(st.getDeparture_time()) >= startSeconds) {
                        BusStop s = stopsById.get(stopId);
                        BusStop n = stopsById.get(seq.get(i + 1).getStop_id());
                        if (s != null && n != null) {
                            bearings.add(calculateBearing(s.getStop_lat(), s.getStop_lon(), n.getStop_lat(), n.getStop_lon()));
                        }
                    }
                }
            }
        }
        return bearings;
    }

    // ------------------ Core: planRoute ------------------

    public List<Journey> planRoute(double latFrom, double lngFrom, double latTo, double lngTo, Calendar startTime) {
        // 1. Load Data từ DAO
        TransitData data = TransitData.loadAll(db);
        Map<String, BusStop> stopsById = new HashMap<>();
        for (BusStop s : data.stops) stopsById.put(s.getStop_id(), s);

        // Nhóm stop_times theo trip để ranking
        Map<String, List<BusStopTimes>> stbt = new HashMap<>();
        for (BusStopTimes st : data.stopTimes) {
            stbt.computeIfAbsent(st.getTrip_id(), k -> new ArrayList<>()).add(st);
        }

        // 2. Tìm bến gần nhất & Ranking
        List<BusStop> nearbyStart = NearestStopFinder.findNearestStops(latFrom, lngFrom, data.stops, 300, 100, 1500);
        List<BusStop> nearbyEnd = NearestStopFinder.findNearestStops(latTo, lngTo, data.stops, 300, 100, 1500);

        int startSeconds = startTime.get(Calendar.HOUR_OF_DAY) * 3600 + startTime.get(Calendar.MINUTE) * 60;

        List<String> rankedStartIds = rankStartStopsDirectional(nearbyStart, stopsById, stbt, latTo, lngTo, startSeconds);
        List<String> endStopIds = new ArrayList<>();
        for (BusStop s : nearbyEnd) endStopIds.add(s.getStop_id());

        // 3. Chạy RAPTOR
        Map<String, List<NearestStopFinder.StopDistance>> nearbyStopMap =
                NearestStopFinder.buildNearbyStopMap(data.stops, latFrom, lngFrom, latTo, lngTo, 300, 100, 1000);

        List<List<Map<String, Object>>> raptorJourneys = raptorRouter.runRaptor(
                data.trips, data.stopTimes, rankedStartIds, endStopIds, startTime, 6, nearbyStopMap, 500.0);

        // 4. Reconstruct với A* (Chuyển đổi các chặng RAPTOR thành Full Journey có tọa độ)
        List<Journey> finalResults = new ArrayList<>();
        Map<Integer, List<Edge>> graph = AStarAlg.buildGraph(data.edges);

        for (List<Map<String, Object>> transitLegs : raptorJourneys) {
            Journey journey = new Journey();
            journey.legs = reconstructFullJourney(data, graph, latFrom, lngFrom, latTo, lngTo, transitLegs);
            finalResults.add(journey);
        }

        return finalResults;
    }

    private List<JourneyLeg> reconstructFullJourney(TransitData data, Map<Integer, List<Edge>> graph,
                                                    double startLat, double startLon, double endLat, double endLon,
                                                    List<Map<String, Object>> transitLegs) {
        List<JourneyLeg> legs = new ArrayList<>();
        if (transitLegs.isEmpty()) return legs;

        // Đi bộ từ vị trí người dùng -> Bến đầu tiên
        String firstStopId = (String) transitLegs.get(0).get("from_stop");
        int startNode = findNearestNode(data.nodes, startLat, startLon);
        int firstBoardNode = data.stopNodeMap.get(firstStopId);

        AStarAlg.AStarResult w1 = AStarAlg.aStarSearch(graph, data.nodes, startNode, firstBoardNode);
        legs.add(new JourneyLeg("walk", w1.path, w1.distance));

        // Các chặng bus và đi bộ chuyển tuyến
        for (int i = 0; i < transitLegs.size(); i++) {
            Map<String, Object> busLegMap = transitLegs.get(i);
            legs.add(new JourneyLeg("bus", busLegMap));

            if (i < transitLegs.size() - 1) {
                String prevAlight = (String) busLegMap.get("to_stop");
                String nextBoard = (String) transitLegs.get(i + 1).get("from_stop");
                if (!prevAlight.equals(nextBoard)) {
                    int n1 = data.stopNodeMap.get(prevAlight);
                    int n2 = data.stopNodeMap.get(nextBoard);
                    AStarAlg.AStarResult walkInter = AStarAlg.aStarSearch(graph, data.nodes, n1, n2);
                    legs.add(new JourneyLeg("walk", walkInter.path, walkInter.distance));
                }
            }
        }

        // Chặng đi bộ cuối cùng: Bến cuối -> Đích
        String lastStopId = (String) transitLegs.get(transitLegs.size() - 1).get("to_stop");
        int lastNode = data.stopNodeMap.get(lastStopId);
        int endNode = findNearestNode(data.nodes, endLat, endLon);
        AStarAlg.AStarResult w2 = AStarAlg.aStarSearch(graph, data.nodes, lastNode, endNode);
        legs.add(new JourneyLeg("walk", w2.path, w2.distance));

        return legs;
    }

    private int findNearestNode(Map<Integer, double[]> nodes, double lat, double lon) {
        int bestId = -1;
        double minD = Double.MAX_VALUE;
        for (Map.Entry<Integer, double[]> entry : nodes.entrySet()) {
            double d = AStarAlg.haversine(lat, lon, entry.getValue()[0], entry.getValue()[1]);
            if (d < minD) {
                minD = d;
                bestId = entry.getKey();
            }
        }
        return bestId;
    }

    private int parseHms(String hms) {
        String[] p = hms.split(":");
        return Integer.parseInt(p[0]) * 3600 + Integer.parseInt(p[1]) * 60 + Integer.parseInt(p[2]);
    }

    // Helper classes cho kết quả
    private static class ScoredStop {
        String stopId; double distance; int penaltySec; double angleDiff;
        ScoredStop(String id, double d, int p, double a) {
            this.stopId = id; this.distance = d; this.penaltySec = p; this.angleDiff = a;
        }
    }
}
