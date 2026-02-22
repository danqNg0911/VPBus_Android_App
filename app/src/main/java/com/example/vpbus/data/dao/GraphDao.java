package com.example.vpbus.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Node;

import java.util.List;

@Dao
public interface GraphDao {
    @Query("SELECT * FROM nodes")
    List<Node> getAllNodes();

    @Query("SELECT * FROM edges")
    List<Edge> getAllEdges();
}
