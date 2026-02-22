package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nodes")
public class Node {
    @PrimaryKey
    @NonNull
    private int id;
    @ColumnInfo(name = "lat")
    @NonNull
    private double latitude;
    @ColumnInfo(name = "lng")
    @NonNull
    private double longitude;

    public Node(int id, double latitude, double longitude){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
