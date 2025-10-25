package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "shapes", primaryKeys = {"shape_id", "shape_pt_sequence"})
public class BusShape {
    @NonNull
    private String shape_id;
    @ColumnInfo(name = "shape_pt_lat")
    @NonNull
    private double shape_pt_lat;
    @ColumnInfo(name = "shape_pt_lon")
    @NonNull
    private double shape_pt_lon;
    @ColumnInfo(name = "shape_pt_sequence")
    @NonNull
    private int shape_pt_sequence;

    public BusShape(String shape_id, double shape_pt_lat, double shape_pt_lon, int shape_pt_sequence) {
        this.shape_id = shape_id;
        this.shape_pt_lat = shape_pt_lat;
        this.shape_pt_lon = shape_pt_lon;
        this.shape_pt_sequence = shape_pt_sequence;
    }

    public String getShape_id() {
        return shape_id;
    }

    public void setShape_id(String shape_id) {
        this.shape_id = shape_id;
    }

    public double getShape_pt_lat() {
        return shape_pt_lat;
    }

    public void setShape_pt_lat(double shape_pt_lat) {
        this.shape_pt_lat = shape_pt_lat;
    }

    public double getShape_pt_lon() {
        return shape_pt_lon;
    }

    public void setShape_pt_lon(double shape_pt_lon) {
        this.shape_pt_lon = shape_pt_lon;
    }

    public int getShape_pt_sequence() {
        return shape_pt_sequence;
    }

    public void setShape_pt_sequence(int shape_pt_sequence) {
        this.shape_pt_sequence = shape_pt_sequence;
    }
}
