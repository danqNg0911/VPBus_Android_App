package com.example.vpbus.service.Router;

import com.example.vpbus.model.Edge;

import java.util.*;

public class AStarAlg {

    // model trả kết quả
    public static class AStarResult {
        public double distance;
        public List<Integer> path;

        public AStarResult(double distance, List<Integer> path) {
            this.distance = distance;
            this.path = path;
        }
    }

    //Helper dùng trong pqueue
    private static class NodeScore implements Comparable<NodeScore> {
        int id;
        double fScore;

        NodeScore(int id, double fScore) {
            this.id = id;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(NodeScore other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    // Tính khoảng cách Haversine
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    //build graph dạng adjency list
    public static Map<Integer, List<Edge>> buildGraph(List<Edge> edges) {
        Map<Integer, List<Edge>> graph = new HashMap<>();
        for (Edge edge : edges) {
            graph.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(new Edge(edge.getFrom(), edge.getTo(), edge.getDistance()));

            // Nếu 2 chiều: thêm chiều ngược
            graph.computeIfAbsent(edge.getTo(), k -> new ArrayList<>()).add(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
        }
        return graph;
    }

    // chạy A* từ start đến goal node
    public static AStarResult aStarSearch(
            Map<Integer, List<Edge>> graph,
            Map<Integer, double[]> nodes, // double[] chứa {lat, lng}
            int start,
            int goal
    ) {
        PriorityQueue<NodeScore> openSet = new PriorityQueue<>();
        openSet.add(new NodeScore(start, 0));

        Map<Integer, Integer> cameFrom = new HashMap<>();
        Map<Integer, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);

        while (!openSet.isEmpty()) {
            NodeScore currentScore = openSet.poll();
            int current = currentScore.id;

            if (current == goal) {
                List<Integer> path = new ArrayList<>();
                int temp = current;
                while (cameFrom.containsKey(temp)) {
                    path.add(temp);
                    temp = cameFrom.get(temp);
                }
                path.add(start);
                Collections.reverse(path);

                System.out.println("[LOG] A* path found from " + start + " to " + goal +
                        ", length=" + path.size() + ", nodes=" + path);
                return new AStarResult(gScore.get(goal), path);
            }

            List<Edge> neighbors = graph.getOrDefault(current, new ArrayList<>());
            for (Edge edge : neighbors) {
                int neighbor = edge.getTo();
                double tentativeG = gScore.get(current) + edge.getDistance();

                if (!gScore.containsKey(neighbor) || tentativeG < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);

                    double h = haversine(nodes.get(neighbor)[0], nodes.get(neighbor)[1],
                            nodes.get(goal)[0], nodes.get(goal)[1]);
                    double fScore = tentativeG + h;
                    openSet.add(new NodeScore(neighbor, fScore));
                }
            }
        }

        System.out.println("[LOG] A* no path from " + start + " to " + goal + ", trying fallback walk link...");
        return new AStarResult(Double.POSITIVE_INFINITY, new ArrayList<>());
    }

     // Tìm cặp node gần nhất để đi bộ nối 2 cụm khi không có path.
    public static AStarResult fallbackWalkLink(Map<Integer, List<Edge>> graph, Map<Integer, double[]> nodes, int start, int goal) {
        List<Integer> startNeighbors = new ArrayList<>();
        if (graph.containsKey(start)) {
            for (Edge e : graph.get(start)) startNeighbors.add(e.getTo());
        }

        List<Integer> goalNeighbors = new ArrayList<>();
        if (graph.containsKey(goal)) {
            for (Edge e : graph.get(goal)) goalNeighbors.add(e.getTo());
        }

        Integer bestA = null, bestB = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (int a1 : startNeighbors) {
            for (int b1 : goalNeighbors) {
                double dist = haversine(nodes.get(a1)[0], nodes.get(a1)[1], nodes.get(b1)[0], nodes.get(b1)[1]);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestA = a1;
                    bestB = b1;
                }
            }
        }

        if (bestA != null && bestB != null) {
            System.out.printf("[LOG] Fallback: walk %.1fm from %d to %d%n", bestDist, bestA, bestB);
            List<Integer> path = Arrays.asList(start, bestA, bestB, goal);
            return new AStarResult(bestDist, path);
        } else {
            System.out.println("[LOG] No path and no fallback found.");
            return new AStarResult(Double.POSITIVE_INFINITY, new ArrayList<>());
        }
    }
}
