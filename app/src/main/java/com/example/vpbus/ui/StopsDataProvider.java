package com.example.vpbus.ui;

import com.example.vpbus.model.BusStop;

import java.util.List;

public interface StopsDataProvider {
    List<BusStop> getNearbyStops();
    List<BusStop> getFavoriteStops();
}
