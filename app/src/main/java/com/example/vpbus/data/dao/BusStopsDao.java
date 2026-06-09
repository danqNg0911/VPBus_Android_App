package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.StopNodeMap;

import java.util.List;

@Dao
public interface BusStopsDao {
    @Query("SELECT * FROM stops")
    List<BusStop> getAllStops();

    @Query("SELECT * FROM stops WHERE stop_id = :stopId LIMIT 1")
    BusStop getStopById(String stopId);

    @Query("SELECT s.* FROM stops s JOIN stop_times st ON s.stop_id = st.stop_id WHERE st.trip_id = :tripId ORDER BY st.stop_sequence ASC")
    List<BusStop> getStopsByTrip(String tripId);

    @Query("SELECT stop_id, node_id FROM stop_node_map")
    List<StopNodeMap> getAllStopNodeMappings();

}
