package com.example.vpbus.service.Router;

import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Journey;
import com.example.vpbus.model.JourneyLeg;
import com.example.vpbus.model.JourneySummary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransitPlanner {

    private final AppDatabase db;
    private final RaptorAlg raptorRouter;

    public TransitPlanner(AppDatabase db) {
        this.db = db;
        this.raptorRouter = new RaptorAlg();
    }

    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        return NearestStopFinder.bearing(lat1, lon1, lat2, lon2);
    }

    private double calculateAngleDiff(double a, double b) {
        return NearestStopFinder.angleDifference(a, b);
    }

    private List<String> rankStartStopsDirectional(
            List<BusStop> nearbyStartStops,
            Map<String, BusStop> stopsById,
            Map<String, List<BusStopTimes>> stopTimesByTrip,
            double destLat,
            double destLon,
            int startSeconds
    ) {
        if (nearbyStartStops.isEmpty()) return new ArrayList<>();

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

            List<Double> departBearings = getDepartingBearings(sid, stopTimesByTrip, stopsById, startSeconds);
            if (departBearings.isEmpty()) {
                scored.add(new ScoredStop(sid, baseDist, 600, 999.0));
                continue;
            }

            double bestDiff = Double.MAX_VALUE;
            for (double bearing : departBearings) {
                bestDiff = Math.min(bestDiff, calculateAngleDiff(bearing, wantBearing));
            }

            int penaltySec = 0;
            if (bestDiff > 110.0) continue;
            if (bestDiff > 70.0) penaltySec += 120;
            if (preferredSide != null && !sid.startsWith(preferredSide)) penaltySec += 60;

            scored.add(new ScoredStop(sid, baseDist, penaltySec, bestDiff));
        }

        scored.sort(Comparator.comparingDouble(item -> item.distance + item.penaltySec * 1.4));

        List<String> result = new ArrayList<>();
        for (ScoredStop ss : scored) result.add(ss.stopId);
        return result;
    }

    private List<Double> getDepartingBearings(
            String stopId,
            Map<String, List<BusStopTimes>> stopTimesByTrip,
            Map<String, BusStop> stopsById,
            int startSeconds
    ) {
        List<Double> bearings = new ArrayList<>();
        for (List<BusStopTimes> seq : stopTimesByTrip.values()) {
            for (int i = 0; i < seq.size() - 1; i++) {
                BusStopTimes st = seq.get(i);
                if (!st.getStop_id().equals(stopId)) continue;
                if (parseHms(st.getDeparture_time()) < startSeconds) continue;

                BusStop s = stopsById.get(stopId);
                BusStop n = stopsById.get(seq.get(i + 1).getStop_id());
                if (s != null && n != null) {
                    bearings.add(calculateBearing(s.getStop_lat(), s.getStop_lon(), n.getStop_lat(), n.getStop_lon()));
                }
            }
        }
        return bearings;
    }

    public List<Journey> planRoute(double latFrom, double lngFrom, double latTo, double lngTo, Calendar startTime) {
        TransitData data = TransitData.loadAll(db);

        List<BusStop> nearbyStart = NearestStopFinder.findNearestStops(latFrom, lngFrom, data.stops, 300, 100, 1500);
        List<BusStop> nearbyEnd = NearestStopFinder.findNearestStops(latTo, lngTo, data.stops, 300, 100, 1500);

        int startSeconds = startTime.get(Calendar.HOUR_OF_DAY) * 3600 + startTime.get(Calendar.MINUTE) * 60;
        List<String> rankedStartIds = rankStartStopsDirectional(
                nearbyStart,
                data.stopsById,
                data.stopTimesByTrip,
                latTo,
                lngTo,
                startSeconds
        );
        if (rankedStartIds.isEmpty()) {
            for (BusStop stop : nearbyStart) {
                rankedStartIds.add(stop.getStop_id());
            }
        }

        List<String> endStopIds = new ArrayList<>();
        for (BusStop s : nearbyEnd) endStopIds.add(s.getStop_id());

        Map<String, List<NearestStopFinder.StopDistance>> nearbyStopMap =
                NearestStopFinder.buildNearbyStopMap(data.stops, latFrom, lngFrom, latTo, lngTo, 300, 100, 1000);

        List<List<Map<String, Object>>> raptorJourneys = raptorRouter.runRaptor(
                data,
                rankedStartIds,
                endStopIds,
                startTime,
                3,
                nearbyStopMap,
                500.0
        );
        if (raptorJourneys.isEmpty()) {
            Calendar morningFallback = (Calendar) startTime.clone();
            morningFallback.set(Calendar.HOUR_OF_DAY, 8);
            morningFallback.set(Calendar.MINUTE, 0);
            morningFallback.set(Calendar.SECOND, 0);
            morningFallback.set(Calendar.MILLISECOND, 0);
            raptorJourneys = raptorRouter.runRaptor(
                    data,
                    rankedStartIds,
                    endStopIds,
                    morningFallback,
                    3,
                    nearbyStopMap,
                    500.0
            );
            startSeconds = 8 * 3600;
        }

        List<Journey> finalResults = new ArrayList<>();
        for (List<Map<String, Object>> transitLegs : raptorJourneys) {
            List<Map<String, Object>> mergedTransitLegs = mergeBusLegs(transitLegs);
            if (!isValidTransitResult(mergedTransitLegs)) continue;

            Journey journey = new Journey();
            journey.legs = reconstructFullJourney(data, data.graph, latFrom, lngFrom, latTo, lngTo, mergedTransitLegs);
            if (journey.legs.isEmpty()) continue;

            journey.summary = buildSummary(journey, startSeconds);
            finalResults.add(journey);
        }

        return rankAndDeduplicate(finalResults);
    }

    private List<JourneyLeg> reconstructFullJourney(
            TransitData data,
            Map<Integer, List<Edge>> graph,
            double startLat,
            double startLon,
            double endLat,
            double endLon,
            List<Map<String, Object>> transitLegs
    ) {
        List<JourneyLeg> legs = new ArrayList<>();
        if (transitLegs.isEmpty()) return legs;

        String firstStopId = (String) transitLegs.get(0).get("from_stop");
        int startNode = findNearestNode(data.nodes, startLat, startLon);
        Integer firstBoardNode = data.stopNodeMap.get(firstStopId);
        if (startNode < 0 || firstBoardNode == null) return legs;

        AStarAlg.AStarResult firstWalk = findWalkPath(graph, data.nodes, startNode, firstBoardNode);
        legs.add(new JourneyLeg("walk", firstWalk.path, firstWalk.distance));

        for (int i = 0; i < transitLegs.size(); i++) {
            Map<String, Object> busLegMap = transitLegs.get(i);
            legs.add(new JourneyLeg("bus", busLegMap));

            if (i < transitLegs.size() - 1) {
                String prevAlight = (String) busLegMap.get("to_stop");
                String nextBoard = (String) transitLegs.get(i + 1).get("from_stop");
                if (!prevAlight.equals(nextBoard)) {
                    Integer n1 = data.stopNodeMap.get(prevAlight);
                    Integer n2 = data.stopNodeMap.get(nextBoard);
                    if (n1 == null || n2 == null) return new ArrayList<>();
                    AStarAlg.AStarResult walkInter = findWalkPath(graph, data.nodes, n1, n2);
                    if (walkInter.distance > 500.0) return new ArrayList<>();
                    legs.add(new JourneyLeg("walk", walkInter.path, walkInter.distance));
                }
            }
        }

        String lastStopId = (String) transitLegs.get(transitLegs.size() - 1).get("to_stop");
        Integer lastNode = data.stopNodeMap.get(lastStopId);
        int endNode = findNearestNode(data.nodes, endLat, endLon);
        if (lastNode == null || endNode < 0) return new ArrayList<>();

        AStarAlg.AStarResult lastWalk = findWalkPath(graph, data.nodes, lastNode, endNode);
        legs.add(new JourneyLeg("walk", lastWalk.path, lastWalk.distance));

        return legs;
    }

    private AStarAlg.AStarResult findWalkPath(Map<Integer, List<Edge>> graph, Map<Integer, double[]> nodes, int start, int goal) {
        AStarAlg.AStarResult result = AStarAlg.aStarSearch(graph, nodes, start, goal);
        if (!Double.isFinite(result.distance)) {
            result = AStarAlg.fallbackWalkLink(graph, nodes, start, goal);
        }
        return result;
    }

    private List<Map<String, Object>> mergeBusLegs(List<Map<String, Object>> transitLegs) {
        List<Map<String, Object>> merged = new ArrayList<>();
        for (Map<String, Object> leg : transitLegs) {
            if (merged.isEmpty()) {
                merged.add(new HashMap<>(leg));
                continue;
            }

            Map<String, Object> previous = merged.get(merged.size() - 1);
            String prevTrip = (String) previous.get("trip_id");
            String currTrip = (String) leg.get("trip_id");
            String prevRoute = (String) previous.get("route_id");
            String currRoute = (String) leg.get("route_id");
            String prevTo = (String) previous.get("to_stop");
            String currFrom = (String) leg.get("from_stop");

            boolean sameVehicle = prevTrip != null && prevTrip.equals(currTrip);
            boolean sameRouteContinuation = prevRoute != null && prevRoute.equals(currRoute)
                    && prevTo != null && prevTo.equals(currFrom);

            if (sameVehicle || sameRouteContinuation) {
                previous.put("to_stop", leg.get("to_stop"));
                previous.put("arrive_time", leg.get("arrive_time"));
            } else {
                merged.add(new HashMap<>(leg));
            }
        }
        return merged;
    }

    private boolean isValidTransitResult(List<Map<String, Object>> transitLegs) {
        if (transitLegs.isEmpty() || transitLegs.size() > 3) return false;

        Set<String> seenTransitions = new HashSet<>();
        Set<String> seenRoutes = new HashSet<>();
        String previousRoute = null;

        for (Map<String, Object> leg : transitLegs) {
            String routeId = (String) leg.get("route_id");
            String fromStop = (String) leg.get("from_stop");
            String toStop = (String) leg.get("to_stop");

            if (routeId == null || fromStop == null || toStop == null || fromStop.equals(toStop)) return false;
            if (!seenTransitions.add(fromStop + ">" + toStop)) return false;
            if (!routeId.equals(previousRoute) && seenRoutes.contains(routeId)) return false;

            seenRoutes.add(routeId);
            previousRoute = routeId;
        }
        return true;
    }

    private JourneySummary buildSummary(Journey journey, int requestedStartSec) {
        int firstDepart = Integer.MAX_VALUE;
        int lastArrive = requestedStartSec;
        int busLegCount = 0;
        double walkingDistance = 0.0;

        for (JourneyLeg leg : journey.legs) {
            if ("walk".equals(leg.getType())) {
                walkingDistance += leg.getDistance();
            } else if ("bus".equals(leg.getType())) {
                busLegCount++;
                firstDepart = Math.min(firstDepart, parseHms((String) leg.getBusInfo().get("depart_time")));
                lastArrive = Math.max(lastArrive, parseHms((String) leg.getBusInfo().get("arrive_time")));
            }
        }

        int departSec = firstDepart == Integer.MAX_VALUE ? requestedStartSec : firstDepart;
        int finalWalkSec = (int) Math.round((walkingDistance / 80.0) * 60.0);
        int arriveSec = lastArrive + finalWalkSec;

        return new JourneySummary(
                departSec,
                arriveSec,
                Math.max(0, arriveSec - requestedStartSec),
                Math.max(0, busLegCount - 1),
                busLegCount,
                walkingDistance,
                busLegCount * 10000
        );
    }

    private List<Journey> rankAndDeduplicate(List<Journey> journeys) {
        journeys.sort(Comparator
                .comparingInt((Journey j) -> j.summary.arriveSec)
                .thenComparingInt(j -> j.summary.transferCount)
                .thenComparingDouble(j -> j.summary.walkingDistanceMeters));

        Map<String, Journey> unique = new LinkedHashMap<>();
        for (Journey journey : journeys) {
            String signature = buildSignature(journey);
            Journey existing = unique.get(signature);
            if (existing == null || journey.summary.arriveSec < existing.summary.arriveSec) {
                unique.put(signature, journey);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private String buildSignature(Journey journey) {
        StringBuilder builder = new StringBuilder();
        for (JourneyLeg leg : journey.legs) {
            if (!"bus".equals(leg.getType())) continue;
            builder.append(leg.getBusInfo().get("route_id"))
                    .append(':')
                    .append(leg.getBusInfo().get("from_stop"))
                    .append('>')
                    .append(leg.getBusInfo().get("to_stop"))
                    .append('|');
        }
        return builder.toString();
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

    private static class ScoredStop {
        String stopId;
        double distance;
        int penaltySec;
        double angleDiff;

        ScoredStop(String id, double d, int p, double a) {
            this.stopId = id;
            this.distance = d;
            this.penaltySec = p;
            this.angleDiff = a;
        }
    }
}
