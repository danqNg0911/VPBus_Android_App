package com.example.vpbus.model;

public class Destination extends LocationPoint {
    public Destination(double latitude, double longitude, String searchText, BusStop selectedStop) {
        super(latitude, longitude, searchText, selectedStop);
    }

    public double distanceTo(Location o) {
        return Math.sqrt(Math.pow((this.latitude - o.getLatitude()), 2) + Math.pow((this.longitude - o.getLongitude()), 2));
    }
}
