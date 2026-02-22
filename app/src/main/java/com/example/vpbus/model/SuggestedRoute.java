package com.example.vpbus.model;

import java.util.List;

public class SuggestedRoute {
    private List<JourneyLeg> journeyLegs;
    double totalEstimatedTime;
    int totalPrice;

    public SuggestedRoute(List<JourneyLeg> journeyLegs) {
        this.journeyLegs = journeyLegs;
    }

    public double getTotalEstimatedTime() {
        return totalEstimatedTime;
    }

    public void setTotalEstimatedTime(double totalEstimatedTime) {
        this.totalEstimatedTime = totalEstimatedTime;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Thay getter = add + remove
     * @param e
     */
    public void addLegs(JourneyLeg e) {
        journeyLegs.add(e);
    }

    public void removeLegs(JourneyLeg e) {
        journeyLegs.remove(e);
    }
    public List<JourneyLeg> getLegs() {
        return journeyLegs;
    }
}
