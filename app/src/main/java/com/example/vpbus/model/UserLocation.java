package com.example.vpbus.model;

public class UserLocation implements Location {
    private double latitude;
    private double longitude;
    private double timestamp;

    public UserLocation() {
    }

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public double distanceTo(Location o) {
        return Math.sqrt(Math.pow((this.latitude - o.getLatitude()), 2) + Math.pow((this.longitude - o.getLongitude()), 2));
    }
}
