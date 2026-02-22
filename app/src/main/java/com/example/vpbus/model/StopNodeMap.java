package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stop_node_map")
public class StopNodeMap {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "stop_id")
    private String stopId;
    @ColumnInfo(name = "node_id")
    @NonNull
    private int nodeId;

    public StopNodeMap(String stopId, int nodeId) {
        this.stopId = stopId;
        this.nodeId = nodeId;
    }

    public String getStopId() {
        return stopId;
    }

    public int getNodeId() {
        return nodeId;
    }

}
