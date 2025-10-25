package com.example.vpbus.model;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "routes")
public class BusRoute {
    @PrimaryKey
    @NonNull
    private int route_id;
    @ColumnInfo(name = "route_short_name")
    private String short_name;
    @ColumnInfo(name = "route_long_name")
    private String long_name;
    @ColumnInfo(name = "price")
    @NonNull
    private int price;
    @ColumnInfo(name = "route_type")
    private int route_type;

    public BusRoute(int route_id, String short_name, String long_name, int price, int route_type) {
        this.route_id = route_id;
        this.long_name = long_name;
        this.short_name = short_name;
        this.price = price;
        this.route_type = route_type;
    }

    public int getRoute_id() {
        return route_id;
    }

    public void setRoute_id(int route_id) {
        this.route_id = route_id;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getRoute_type() {
        return route_type;
    }

    public void setRoute_type(int route_type) {
        this.route_type = route_type;
    }
}