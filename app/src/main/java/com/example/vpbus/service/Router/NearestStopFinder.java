package com.example.vpbus.service.Router;
import com.example.vpbus.model.BusStop;

import java.util.*;

public class NearestStopFinder {

    /**
     * Tính khoảng cách giữa 2 tọa độ (đơn vị: mét)
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Bán kính Trái Đất (m)
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dphi / 2) * Math.sin(dphi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(dlambda / 2) * Math.sin(dlambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Tính bearing (hướng) từ điểm 1 đến điểm 2 (0° = Bắc, 90° = Đông)
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double lambda1 = Math.toRadians(lon1);
        double lambda2 = Math.toRadians(lon2);

        double dLon = lambda2 - lambda1;
        double x = Math.sin(dLon) * Math.cos(phi2);
        double y = Math.cos(phi1) * Math.sin(phi2) -
                Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLon);

        double brng = Math.atan2(x, y);
        return (Math.toDegrees(brng) + 360) % 360;
    }

    /**
     * Sai khác góc nhỏ nhất giữa 2 góc (độ)
     */
    public static double angleDifference(double angle1, double angle2) {
        double diff = Math.abs(angle1 - angle2) % 360;
        return diff <= 180 ? diff : 360 - diff;
    }

    /**
     * Tìm tất cả các bến trong bán kính radius mét từ vị trí (lat, lng)
     * Radius mở rộng dần nếu không tìm thấy bến nào.
     */
    public static List<BusStop> findNearestStops(
            double lat,
            double lng,
            List<BusStop> stops,
            double startRadius,
            double step,
            double maxRadius
    ) {
        double radius = startRadius;
        List<BusStop> nearby = new ArrayList<>();

        while (radius <= maxRadius) {
            nearby.clear();
            for (BusStop stop : stops) {
                double dist = haversine(lat, lng, stop.getStop_lat(), stop.getStop_lon());
                if (dist <= radius) {
                    BusStop s = stop.clone();
                    s.distance = dist;
                    nearby.add(s);
                }
            }

            if (!nearby.isEmpty()) {
                break;
            }
            radius += step;
        }

        // Sắp xếp theo khoảng cách tăng dần
        nearby.sort(Comparator.comparingDouble(s -> s.distance));

        System.out.println("[LOG] Found " + nearby.size() + " stops within " + radius + "m of (" + lat + ", " + lng + ")");
        if (!nearby.isEmpty()) {
            System.out.printf("[LOG] Nearest stop: %s (%.1fm)%n", nearby.get(0).getStop_name(), nearby.get(0).distance);
        }

        return nearby;
    }

    /**
     * Xây dựng bản đồ các bến lân cận (dùng cho chuyển tuyến/đi bộ giữa các trạm)
     */
    public static Map<String, List<StopDistance>> buildNearbyStopMap(
            List<BusStop> stops,
            Double startLat, Double startLon,
            Double endLat, Double endLon,
            double startRadius, double step, double maxRadius
    ) {
        Map<String, List<StopDistance>> nearbyStopMap = new HashMap<>();

        Double bearingToDest = null;
        if (startLat != null && startLon != null && endLat != null && endLon != null) {
            bearingToDest = bearing(startLat, startLon, endLat, endLon);
        }

        for (BusStop stop : stops) {
            String stopId = stop.getStop_id();
            double radius = startRadius;
            List<StopDistance> nearby = new ArrayList<>();

            while (radius <= maxRadius) {
                nearby.clear();
                for (BusStop other : stops) {
                    if (other.getStop_id().equals(stopId)) continue;

                    double dist = haversine(stop.getStop_lat(), stop.getStop_lon(), other.getStop_lat(), other.getStop_lon());
                    if (dist <= radius) {
                        if (bearingToDest != null) {
                            double stopBearing = bearing(stop.getStop_lat(), stop.getStop_lon(), other.getStop_lat(), other.getStop_lon());
                            double angleDiff = Math.min(Math.abs(stopBearing - bearingToDest), 360 - Math.abs(stopBearing - bearingToDest));

                            if (angleDiff > 120) continue;
                            if (angleDiff > 60 && angleDiff <= 120) {
                                dist += 100; // penalty
                            }
                        }
                        nearby.add(new StopDistance(other.getStop_id(), dist));
                    }
                }

                if (!nearby.isEmpty()) break;
                radius += step;
            }

            nearby.sort(Comparator.comparingDouble(sd -> sd.distance));
            nearbyStopMap.put(stopId, nearby);
        }

        return nearbyStopMap;
    }

    // Helper class cho kết quả trả về của buildNearbyStopMap
    public static class StopDistance {
        public String stopId;
        public double distance;
        public StopDistance(String id, double d) { this.stopId = id; this.distance = d; }
    }
}

