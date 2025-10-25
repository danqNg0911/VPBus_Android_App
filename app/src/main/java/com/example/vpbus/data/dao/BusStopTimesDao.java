package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.BusStop;

import java.util.List;

@Dao
public interface BusStopTimesDao {
    @Query(
            "SELECT s.* FROM stops s " +
                    "JOIN stop_times st ON s.stop_id = st.stop_id " +
                    "WHERE st.trip_id = :tripId " +
                    "ORDER BY st.stop_sequence ASC"
    )
    List<BusStop> getStopsByTrip(String tripId);
}
