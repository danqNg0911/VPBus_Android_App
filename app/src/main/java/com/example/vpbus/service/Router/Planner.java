package com.example.vpbus.service.Router;
import java.util.*;

public class Planner {

    public static List<Map<String, Object>> planRoute(
            String dbPath,
            double latFrom, double lngFrom,
            double latTo, double lngTo,
            String startTime) {

        Map<String, Object> data = DataLoader.loadDataFromDb(dbPath);

        List<String> fromStops = Arrays.asList("S1", "S2");
        List<String> toStops = Arrays.asList("S99");

        List<List<Map<String, Object>>> raptorJourneys = RaptorAlg.runRaptor(
                (List<Map<String, Object>>) data.get("trips"),
                (List<Map<String, Object>>) data.get("stop_times"),
                (Map<String, Integer>) data.get("stop_node_map"),
                fromStops,
                toStops,
                startTime,
                3,
                new HashMap<>(),
                500.0
        );

        Map<String, List<String>> graph = AStarAlg.buildGraph(
                (List<Map<String, Object>>) data.get("edges"));

        List<Map<String, Object>> results = new ArrayList<>();

        for (List<Map<String, Object>> legs : raptorJourneys) {
            Map<String, Object> res = new HashMap<>();
            res.put("legs", legs);
            results.add(res);
        }
        return results;
    }
}
