package com.example.vpbus.service.Router;
import java.util.*;

public class DataLoader {

    public static Map<String, Object> loadDataFromDb(String dbPath) {
        // ⚙️ Mock dữ liệu DB
        Map<String, Object> data = new HashMap<>();
        data.put("trips", new ArrayList<Map<String, Object>>());
        data.put("stop_times", new ArrayList<Map<String, Object>>());
        data.put("stop_node_map", new HashMap<String, Integer>());
        data.put("edges", new ArrayList<Map<String, Object>>());
        return data;
    }
}
