package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "edges")
public class Edge {
    @PrimaryKey(autoGenerate = true)
    public long e_id;
    @NonNull
    @ColumnInfo(name = "from_id")
    private int from;
    @NonNull
    @ColumnInfo(name = "to_id")
    private int to;
    @NonNull
    @ColumnInfo(name = "distance")
    private double distance;

    public Edge(int from, int to, double distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public double getDistance() {
        return distance;
    }

}
