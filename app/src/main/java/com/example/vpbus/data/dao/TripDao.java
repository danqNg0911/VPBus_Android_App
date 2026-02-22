package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.Trip;

import java.util.List;

@Dao
public interface TripDao {

    @Query("SELECT * FROM trips")
    List<Trip> getAllTrips();

    @Query("SELECT trip_id FROM trips WHERE route_id = :routeShortName")
    List<String> getTripIdsByShortName(String routeShortName);

    @Query("SELECT trip_id FROM trips WHERE route_id = :routeShortName AND direction_id = :direction")
    List<String> getTripIdsByShortNameAndDirection(String routeShortName, int direction);

    @Query("SELECT DISTINCT shape_id FROM trips WHERE route_id = :routeId")
    List<String> getShapeIdsByRoute(String routeId);

    @Query("SELECT shape_id FROM trips WHERE trip_id = :tripId LIMIT 1")
    String getShapeIdByTrip(String tripId);

    @Query("SELECT * FROM stop_times ORDER BY trip_id, stop_sequence")
    List<BusStopTimes> getAllStopTimes();
}
