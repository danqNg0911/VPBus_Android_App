package com.example.vpbus.model;

import java.util.List;

public class SuggestedRoute {
    private List<Leg> legs;
    double totalEstimatedTime;
    int totalPrice;

    public SuggestedRoute(List<Leg> legs) {
        this.legs = legs;
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
    public void addLegs(Leg e) {
        legs.add(e);
    }

    public void removeLegs(Leg e) {
        legs.remove(e);
    }
    public List<Leg> getLegs() {
        return legs;
    }
}
