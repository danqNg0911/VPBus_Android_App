package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stops")
public class BusStop {
    @PrimaryKey
    @NonNull
    private String stop_id;
    @ColumnInfo(name = "stop_name")
    private String stop_name;
    @ColumnInfo(name = "stop_lat")
    @NonNull
    private double stop_lat;
    @ColumnInfo(name = "stop_lon")
    @NonNull
    private double stop_lon;

    //public BusStop() {}

    public BusStop(String stop_id, String stop_name, double stop_lat, double stop_lon) {
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.stop_lat = stop_lat;
        this.stop_lon = stop_lon;
    }

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public double getStop_lat() {
        return stop_lat;
    }

    public void setStop_lat(double stop_lat) {
        this.stop_lat = stop_lat;
    }

    public double getStop_lon() {
        return stop_lon;
    }

    public void setStop_lon(double stop_lon) {
        this.stop_lon = stop_lon;
    }

}
