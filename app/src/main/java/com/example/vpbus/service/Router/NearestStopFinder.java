package com.example.vpbus.service.Router;
import java.util.*;

public class NearestStopFinder {

    public static List<Map<String, Object>> findNearestStops(double lat, double lng, List<Map<String, Object>> stops) {
        // Mock 2 stops
        Map<String, Object> s1 = new HashMap<>();
        s1.put("stop_id", "S1");
        s1.put("distance", 100.0);
        Map<String, Object> s2 = new HashMap<>();
        s2.put("stop_id", "S2");
        s2.put("distance", 200.0);
        return Arrays.asList(s1, s2);
    }
}
