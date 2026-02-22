package com.example.vpbus.model;

public class RaptorParentInfo {
    public String mode; // "bus" hoặc "walk"
    public int prevRound;
    public String prevStop;
    public String tripId;
    public String routeId;
    public String fromStop;
    public String toStop;
    public int departTimeSec;
    public int arriveTimeSec;
    public double distM;
}
