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

    @Query("SELECT printf('%08s', departure_time) FROM stop_times " +
            "INNER JOIN trips ON stop_times.trip_id = trips.trip_id " +
            "WHERE trips.route_id = :shortName " +
            "AND SUBSTR(stop_times.trip_id, -1, 1) = '' || :directionId " + // Ép directionId thành chuỗi
            "AND stop_times.stop_sequence = 1 " +
            "ORDER BY printf('%08s', departure_time) ASC")
    List<String> getDepartureTimes(String shortName, int directionId);

}
