package com.example.vpbus.service.Router;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.BusStopTimes;
import com.example.vpbus.model.BusShape;
import com.example.vpbus.model.Edge;
import com.example.vpbus.model.Node;
import com.example.vpbus.model.StopNodeMap;
import com.example.vpbus.model.Trip;

import java.util.*;

public class TransitData {
    private static volatile TransitData cachedData;

    public List<BusStop> stops;
    public Map<String, BusStop> stopsById = new HashMap<>();
    public Map<String, Integer> stopNodeMap = new HashMap<>();
    public Map<Integer, double[]> nodes = new HashMap<>();
    public List<Edge> edges;
    public Map<Integer, List<Edge>> graph = new HashMap<>();
    public List<Trip> trips;
    public Map<String, Trip> tripsById = new HashMap<>();
    public List<BusStopTimes> stopTimes;
    public Map<String, List<BusStopTimes>> stopTimesByTrip = new HashMap<>();
    public Map<String, List<BusShape>> shapesById = new HashMap<>();

    public static TransitData loadAll(AppDatabase db) {
        if (cachedData != null) {
            return cachedData;
        }

        TransitData data = new TransitData();

        // Load stops
        data.stops = db.busStopsDao().getAllStops();
        for (BusStop s : data.stops) data.stopsById.put(s.getStop_id(), s);

        // Load stop_node_map (Chuyển List thành Map như Python)
        List<StopNodeMap> mappings = db.busStopsDao().getAllStopNodeMappings();
        for (StopNodeMap m : mappings) data.stopNodeMap.put(m.getStopId(), m.getNodeId());

        // Load nodes (Chuyển List thành Map id -> [lat, lng])
        List<Node> nodeList = db.graphDao().getAllNodes();
        for (Node n : nodeList) data.nodes.put(n.getId(), new double[]{n.getLatitude(), n.getLongitude()});

        // Load edges, trips, stop_times
        data.edges = db.graphDao().getAllEdges();
        data.graph = AStarAlg.buildGraph(data.edges);

        data.trips = db.tripDao().getAllTrips();
        for (Trip trip : data.trips) data.tripsById.put(trip.getTrip_id(), trip);

        data.stopTimes = db.tripDao().getAllStopTimes();
        for (BusStopTimes st : data.stopTimes) {
            data.stopTimesByTrip.computeIfAbsent(st.getTrip_id(), k -> new ArrayList<>()).add(st);
        }
        for (List<BusStopTimes> tripStopTimes : data.stopTimesByTrip.values()) {
            tripStopTimes.sort(Comparator.comparingInt(BusStopTimes::getStop_sequence));
        }

        List<BusShape> shapes = db.busShapeDao().getAllShape();
        for (BusShape shape : shapes) {
            data.shapesById.computeIfAbsent(shape.getShape_id(), k -> new ArrayList<>()).add(shape);
        }
        for (List<BusShape> shapePoints : data.shapesById.values()) {
            shapePoints.sort(Comparator.comparingInt(BusShape::getShape_pt_sequence));
        }

        cachedData = data;
        return data;
    }
}
