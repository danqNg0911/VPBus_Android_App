package com.example.vpbus.model;
public class Leg {

    public static final double WALK_SPEED = 4.0;

    private String type;
    private BusRoute route;
    private Location start;
    private Location end;
    private double estimatedTime;

    // Constructor
    public Leg(String type, Location start, Location end, BusRoute route) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.route = route;
        this.estimatedTime = calculateEstimatedTime();
    }


    public double distance() {
        double dx = start.getLatitude() - end.getLatitude();
        double dy = start.getLongitude() - end.getLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }


    public double calculateEstimatedTime() {
        double dist = distance();
        if ("WALK".equalsIgnoreCase(type)) {
            return (dist / WALK_SPEED) * 60; // chuyển thành phút
        } else if ("BUS".equalsIgnoreCase(type) && route != null) {
            return (dist / 27) * 60;
        }
        return -1; // không xác định được thời gian
    }

    public String getType() {
        return type;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public BusRoute getRoute() {
        return route;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }
}
