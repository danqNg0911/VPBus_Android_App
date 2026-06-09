package com.example.vpbus.ui;

import com.example.vpbus.model.Journey;

public class JourneySelectionStore {
    private static Journey selectedJourney;
    private static double startLat;
    private static double startLon;
    private static double endLat;
    private static double endLon;

    public static void setSelectedJourney(Journey journey, double fromLat, double fromLon, double toLat, double toLon) {
        selectedJourney = journey;
        startLat = fromLat;
        startLon = fromLon;
        endLat = toLat;
        endLon = toLon;
    }

    public static Journey getSelectedJourney() {
        return selectedJourney;
    }

    public static double getStartLat() {
        return startLat;
    }

    public static double getStartLon() {
        return startLon;
    }

    public static double getEndLat() {
        return endLat;
    }

    public static double getEndLon() {
        return endLon;
    }
}
