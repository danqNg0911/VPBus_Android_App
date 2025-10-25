package com.example.vpbus.model;
import java.util.ArrayList;
import java.util.List;

public class SuggestedRouteProgress extends TripProgress {

    private SuggestedRoute selectedRoute;
    private int currLeg; // chỉ số chặng hiện tại
    private String currStopIDLeg;
    private List<String> visitedStopLeg;

    public SuggestedRouteProgress(int id, double percentage, SuggestedRoute selectedRoute) {
        super(id, percentage);
        this.selectedRoute = selectedRoute;
        this.currLeg = 0;
        this.visitedStopLeg = new ArrayList<>();
    }

    public SuggestedRoute getSelectedRoute() {
        return selectedRoute;
    }

    public void setSelectedRoute(SuggestedRoute selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public int getCurrLeg() {
        return currLeg;
    }

    public void setCurrLeg(int currLeg) {
        this.currLeg = currLeg;
    }

    public String getCurrStopIDLeg() {
        return currStopIDLeg;
    }

    public void setCurrStopIDLeg(String currStopIDLeg) {
        this.currStopIDLeg = currStopIDLeg;
    }

    public List<String> getVisitedStopLeg() {
        return visitedStopLeg;
    }

    public void addVisitedStopLeg(String stopID) {
        visitedStopLeg.add(stopID);
    }
}
