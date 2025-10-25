package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TripDao {
    @Query("SELECT trip_id FROM trips WHERE route_id = :routeShortName")
    List<String> getTripIdsByShortName(String routeShortName);

    @Query("SELECT DISTINCT shape_id FROM trips WHERE route_id = :routeId")
    List<String> getShapeIdsByRoute(String routeId);

    @Query("SELECT shape_id FROM trips WHERE trip_id = :tripId LIMIT 1")
    String getShapeIdByTrip(String tripId);
}
