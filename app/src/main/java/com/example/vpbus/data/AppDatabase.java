package com.example.vpbus.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.vpbus.data.dao.BusRouteDao;
import com.example.vpbus.data.dao.BusShapeDao;
import com.example.vpbus.data.dao.BusStopTimesDao;
import com.example.vpbus.data.dao.BusStopsDao;
import com.example.vpbus.data.dao.GraphDao;
import com.example.vpbus.data.dao.TripDao;
import com.example.vpbus.model.BusRoute;
import com.example.vpbus.model.BusShape;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Node;
import com.example.vpbus.model.StopNodeMap;
import com.example.vpbus.model.Trip;

@Database(entities = {BusRoute.class, BusStop.class, BusShape.class, BusStopTimes.class, Trip.class, Node.class, Edge.class, StopNodeMap.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract BusRouteDao busRouteDao();
    public abstract BusStopsDao busStopsDao();
    public abstract BusShapeDao busShapeDao();
    public abstract BusStopTimesDao busStopTimesDao();
    public abstract TripDao tripDao();
    public abstract GraphDao graphDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "busdatabase"
                            ).createFromAsset("database/walk_graph.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() 
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
