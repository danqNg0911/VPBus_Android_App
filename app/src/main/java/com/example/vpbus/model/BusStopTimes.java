package com.example.vpbus.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "stop_times", primaryKeys = {"schedule_id", "stop_sequence"})
public class BusStopTimes {
    @NonNull
    private int schedule_id;
    @ColumnInfo(name = "arrival_time")
    private String arrival_time;
    @ColumnInfo(name = "departure_time")
    private String departure_time;
    @NonNull
    private int stop_sequence;
    @ColumnInfo(name = "trip_id")
    private String trip_id;
    @ColumnInfo(name = "stop_id")
    private String stop_id;

    public BusStopTimes(int schedule_id, String arrival_time, String departure_time, int stop_sequence, String trip_id, String stop_id) {
        this.schedule_id = schedule_id;
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
        this.stop_sequence = stop_sequence;
        this.trip_id = trip_id;
        this.stop_id = stop_id;
    }

    public int getSchedule_id() {
        return schedule_id;
    }

    public void setSchedule_id(int schedule_id) {
        this.schedule_id = schedule_id;
    }

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public int getStop_sequence() {
        return stop_sequence;
    }

    public void setStop_sequence(int stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }
}