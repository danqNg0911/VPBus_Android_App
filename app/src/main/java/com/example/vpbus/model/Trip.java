package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trips")
public class Trip {
    @ColumnInfo(name = "route_id")
    private String route_id;
    @ColumnInfo(name = "service_id")
    private String service_id;
    @PrimaryKey
    @NonNull
    private String trip_id;
    @ColumnInfo(name = "shape_id")
    private String shape_id;
    @ColumnInfo(name = "direction_id")
    private int direction_id;

    public Trip(String route_id, String service_id, String trip_id, String shape_id, int direction_id) {
        this.route_id = route_id;
        this.service_id = service_id;
        this.trip_id = trip_id;
        this.shape_id = shape_id;
        this.direction_id = direction_id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getShape_id() {
        return shape_id;
    }

    public void setShape_id(String shape_id) {
        this.shape_id = shape_id;
    }

    public int getDirection_id() {
        return direction_id;
    }

    public void setDirection_id(int direction_id) {
        this.direction_id = direction_id;
    }
}
