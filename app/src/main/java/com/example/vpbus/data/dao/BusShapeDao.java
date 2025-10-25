package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.BusShape;

import java.util.List;

@Dao
public interface BusShapeDao {
    @Query("SELECT * FROM shapes")
    List<BusShape> getAllShape();

    @Query(" SELECT * FROM shapes WHERE shape_id = :shapeId ORDER BY shape_pt_sequence ASC")
    List<BusShape> getShapePoints(String shapeId);
}
