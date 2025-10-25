package com.example.vpbus.model;

public  abstract class LocationPoint implements Location {
    double latitude;
    double longitude;
    String searchText;
    BusStop selectedStop;

    public LocationPoint(double latitude, double longitude, String searchText, BusStop selectedStop) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.searchText = searchText;
        this.selectedStop = selectedStop;
    }

    public BusStop getSelectedStop() {
        return selectedStop;
    }

    public void setSelectedStop(BusStop selectedStop) {
        this.selectedStop = selectedStop;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getDistance(Location o) {
        return Math.sqrt(Math.pow((this.latitude - o.getLatitude()), 2) + Math.pow((this.longitude - o.getLongitude()), 2));
    }
}
