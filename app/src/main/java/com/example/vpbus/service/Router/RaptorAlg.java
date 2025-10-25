package com.example.vpbus.service.Router;
import java.util.*;

public class RaptorAlg {

    public static List<List<Map<String, Object>>> runRaptor(
            List<Map<String, Object>> trips,
            List<Map<String, Object>> stopTimes,
            Map<String, Integer> stopNodeMap,
            List<String> fromStops,
            List<String> toStops,
            String startTime,
            int maxTransfers,
            Map<String, List<Object>> nearbyStopMap,
            double walkingThreshold) {

        System.out.println("[LOG] RAPTOR mock run from " + fromStops + " to " + toStops);

        // ⚙️ Mock một kết quả đơn giản
        Map<String, Object> leg = new HashMap<>();
        leg.put("type", "bus");
        leg.put("route_id", "VP01");
        leg.put("from_stop", "S1");
        leg.put("to_stop", "S99");
        leg.put("depart_time", "08:00:00");
        leg.put("arrive_time", "08:45:00");

        List<Map<String, Object>> journey = new ArrayList<>();
        journey.add(leg);

        return Arrays.asList(journey);
    }
}
