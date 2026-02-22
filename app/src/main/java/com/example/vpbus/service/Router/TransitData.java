package com.example.vpbus.service.Router;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Node;
import com.example.vpbus.model.StopNodeMap;
import com.example.vpbus.model.Trip;

import java.util.*;

public class TransitData {

    public List<BusStop> stops;
    public Map<String, Integer> stopNodeMap = new HashMap<>();
    public Map<Integer, double[]> nodes = new HashMap<>();
    public List<Edge> edges;
    public List<Trip> trips;
    public List<BusStopTimes> stopTimes;

    public static TransitData loadAll(AppDatabase db) {
        TransitData data = new TransitData();

        // Load stops
        data.stops = db.busStopsDao().getAllStops();

        // Load stop_node_map (Chuyển List thành Map như Python)
        List<StopNodeMap> mappings = db.busStopsDao().getAllStopNodeMappings();
        for (StopNodeMap m : mappings) data.stopNodeMap.put(m.getStopId(), m.getNodeId());

        // Load nodes (Chuyển List thành Map id -> [lat, lng])
        List<Node> nodeList = db.graphDao().getAllNodes();
        for (Node n : nodeList) data.nodes.put(n.getId(), new double[]{n.getLatitude(), n.getLongitude()});

        // Load edges, trips, stop_times
        data.edges = db.graphDao().getAllEdges();
        data.trips = db.tripDao().getAllTrips();
        data.stopTimes = db.tripDao().getAllStopTimes();

        return data;
    }
}
