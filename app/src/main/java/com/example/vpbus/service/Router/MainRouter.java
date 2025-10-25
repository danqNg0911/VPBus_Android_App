package com.example.vpbus.service.Router;

import android.util.Log;

import java.util.*;

public class MainRouter {
    private double currentLat;
    private double currentLng;
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int minute = now.get(Calendar.MINUTE);

    private final String dbPath = "walk_graph.db";

    public MainRouter(double currentLat, double currentLng) {
        this.currentLat = currentLat;
        this.currentLng = currentLng;
    }

    public List<Map<String, Object>> findRoute(double destLat, double destLng) {
        Log.d("MainRouter", "Bắt đầu tìm đường từ (" + currentLat + "," + currentLng +
                ") → (" + destLat + "," + destLng + ")");

        String currentTime = String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute);

        List<Map<String, Object>> routes = Planner.planRoute(
                dbPath,
                currentLat, currentLng,
                destLat, destLng,
                currentTime
        );

        if (routes == null || routes.isEmpty()) {
            Log.w("MainRouter", "Không tìm thấy tuyến nào phù hợp!");
            return Collections.emptyList();
        }

        for (Map<String, Object> option : routes) {
            List<Map<String, Object>> legs = (List<Map<String, Object>>) option.get("legs");
            for (Map<String, Object> leg : legs) {
                Log.i("MainRouter", "→ Tuyến " + leg.get("route_id") +
                        " từ " + leg.get("from_stop") + " đến " + leg.get("to_stop"));
            }
        }

        return routes;
    }
}
