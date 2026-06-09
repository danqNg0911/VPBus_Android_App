package com.example.vpbus.model;

public class JourneySummary {
    public int departSec;
    public int arriveSec;
    public int durationSec;
    public int transferCount;
    public int busLegCount;
    public double walkingDistanceMeters;
    public int price;

    public JourneySummary(
            int departSec,
            int arriveSec,
            int durationSec,
            int transferCount,
            int busLegCount,
            double walkingDistanceMeters,
            int price
    ) {
        this.departSec = departSec;
        this.arriveSec = arriveSec;
        this.durationSec = durationSec;
        this.transferCount = transferCount;
        this.busLegCount = busLegCount;
        this.walkingDistanceMeters = walkingDistanceMeters;
        this.price = price;
    }
}
