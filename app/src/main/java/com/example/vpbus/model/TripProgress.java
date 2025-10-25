package com.example.vpbus.model;

public abstract class TripProgress {
    int id;
    double percentage;

    public TripProgress(int id, double percentage) {
        this.id = id;
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = Math.max(0.0, Math.min(percentage, 100.0));
    }

}
