package com.example.vpbus.model;

public interface Location {
    double getLatitude();
    double getLongitude();
    double distanceTo(Location o);
}
