package com.example.vpbus.model;

import java.util.List;
import java.util.Map;

public class JourneyLeg {

    private String type;
    private List<Integer> path;
    private double distance;

    private Map<String, Object> busInfo;

    public JourneyLeg(String type, List<Integer> path, double distance) {
        this.type = type;
        this.path = path;
        this.distance = distance;
    }

    public JourneyLeg(String type, Map<String, Object> busInfo) {
        this.type = type;
        this.busInfo = busInfo;
    }

    public String getType() {
        return type;
    }

    public List<Integer> getPath() {
        return path;
    }

    public Map<String, Object> getBusInfo() {
        return busInfo;
    }

    public double getDistance() {
        return distance;
    }
}
