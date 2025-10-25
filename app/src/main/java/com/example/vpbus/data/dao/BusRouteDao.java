package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.BusRoute;

import java.util.List;

@Dao
public interface BusRouteDao {
    @Query("SELECT * FROM routes")
    List<BusRoute> getAllRoute();

    @Query("SELECT * FROM routes WHERE route_short_name = :shortName LIMIT 1")
    BusRoute getRouteByShortName(String shortName);

}
