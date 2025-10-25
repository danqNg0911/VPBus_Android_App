from typing import List, Dict, Tuple
from datetime import datetime
from data_loader import load_data_from_db
from nearest_stop_finder import find_nearest_stops
from a_star import build_graph, a_star_search
from raptor import run_raptor


def reconstruct_full_journey(
    walking_segments: List[Tuple[int, int, float, List[int]]],
    transit_segments: List[Dict]
) -> List[Dict]:
    """
    Ghép các đoạn đi bộ và RAPTOR lại thành một danh sách hành trình liên tiếp
    Mỗi phần là dict: type: 'walk' hoặc 'transit'
    """
    journey = []

    for seg in walking_segments:
        from_node, to_node, dist, path = seg
        journey.append({
            "type": "walk",
            "from_node": from_node,
            "to_node": to_node,
            "distance": dist,
            "path": path
        })

    for tran in transit_segments:
        tran["type"] = "bus"
        journey.append(tran)

    return journey


def plan_route(
    db_path: str,
    lat_from: float,
    lng_from: float,
    lat_to: float,
    lng_to: float,
    start_time: datetime
) -> List[Dict]:
    data = load_data_from_db(db_path)

    # 1. Tìm bến gần điểm đầu/cuối
    nearby_start_stops = find_nearest_stops(lat_from, lng_from, data["stops"])
    nearby_end_stops = find_nearest_stops(lat_to, lng_to, data["stops"])

    if not nearby_start_stops or not nearby_end_stops:
        print("[ERROR] Không tìm thấy bến gần điểm xuất phát hoặc đích")
        return []

    stop_node_map = data["stop_node_map"]  # {stop_id (str): node_id (int)}

    # Truyền danh sách stop_id (str) vào RAPTOR
    start_stop_ids = [stop["stop_id"] for stop in nearby_start_stops]
    end_stop_ids = [stop["stop_id"] for stop in nearby_end_stops]

    # 2. Chạy RAPTOR
    nearby_stop_map = build_nearby_stop_map(data["stops"])

    raptor_result = run_raptor(
        data["trips"],
        data["stop_times"],
        data["stop_node_map"],
        start_stop_ids,
        end_stop_ids,
        start_time,
        max_transfers=6,
        nearby_stop_map=nearby_stop_map,
        walking_threshold=500.0
    )

    if not raptor_result:
        print("[WARN] RAPTOR không tìm được tuyến. (Chưa tích hợp fallback đi bộ toàn tuyến)")
        return []

    # 3. Tìm đoạn đi bộ nối user <-> bến đầu/cuối
    graph = build_graph(data["edges"])
    nodes = data["nodes"]

    best_start = nearby_start_stops[0]  # Gần nhất
    best_end = nearby_end_stops[0]

    # Lấy node ID tương ứng từ stop_id
    start_node = stop_node_map[best_start["stop_id"]]
    end_node = stop_node_map[best_end["stop_id"]]

    # Tìm A* từ user vị trí → node bến đầu
    _, path1 = a_star_search(graph, nodes, find_nearest_node(lat_from, lng_from, nodes), start_node)
    dist1 = sum(
        haversine(nodes[path1[i]][0], nodes[path1[i]][1], nodes[path1[i + 1]][0], nodes[path1[i + 1]][1])
        for i in range(len(path1) - 1)
    )

    # Từ node bến cuối → vị trí đích user
    _, path2 = a_star_search(graph, nodes, end_node, find_nearest_node(lat_to, lng_to, nodes))
    dist2 = sum(
        haversine(nodes[path2[i]][0], nodes[path2[i]][1], nodes[path2[i + 1]][0], nodes[path2[i + 1]][1])
        for i in range(len(path2) - 1)
    )

    walk_segments = [
        (path1[0], path1[-1], dist1, path1),
        (path2[0], path2[-1], dist2, path2)
    ]

    # 4. Gộp tất cả
    # Tạo phương án cho từng kết quả RAPTOR
    results = []
    for transit_segment in raptor_result:
        # Đi bộ đến bến lên
        stop_node_start = stop_node_map[transit_segment["from_stop"]]
        node_start = find_nearest_node(lat_from, lng_from, nodes)
        _, path1 = a_star_search(graph, nodes, node_start, stop_node_start)
        dist1 = sum(haversine(nodes[path1[i]][0], nodes[path1[i]][1], nodes[path1[i+1]][0], nodes[path1[i+1]][1]) for i in range(len(path1) - 1))

        # Đi bộ từ bến xuống đến điểm đích
        stop_node_end = stop_node_map[transit_segment["to_stop"]]
        node_end = find_nearest_node(lat_to, lng_to, nodes)
        _, path2 = a_star_search(graph, nodes, stop_node_end, node_end)
        dist2 = sum(haversine(nodes[path2[i]][0], nodes[path2[i]][1], nodes[path2[i+1]][0], nodes[path2[i+1]][1]) for i in range(len(path2) - 1))

        walk_segments = []
        if path1:
            walk_segments.append((path1[0], path1[-1], dist1, path1))
        else:
            print(f"[WARN] Không tìm được đường đi bộ từ vị trí bắt đầu đến bến lên: {node_start} -> {stop_node_start}")

        if path2:
            walk_segments.append((path2[0], path2[-1], dist2, path2))
        else:
            print(f"[WARN] Không tìm được đường đi bộ từ bến xuống đến điểm đích: {stop_node_end} -> {node_end}")

        full_journey = reconstruct_full_journey(walk_segments, [transit_segment])
        results.append({"legs": full_journey})

    return results



def haversine(lat1, lon1, lat2, lon2):
    """Tính khoảng cách Haversine giữa 2 điểm"""
    import math
    R = 6371000
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlambda/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c


def find_nearest_node(lat: float, lon: float, nodes: Dict[int, Tuple[float, float]]) -> int:
    """Tìm node gần nhất theo toạ độ"""
    min_dist = float("inf")
    best_node = None
    for node_id, (nlat, nlon) in nodes.items():
        d = haversine(lat, lon, nlat, nlon)
        if d < min_dist:
            min_dist = d
            best_node = node_id
    return best_node

def build_nearby_stop_map(stops: List[Dict], threshold: float = 500.0) -> Dict[str, List[Tuple[str, float]]]:
    nearby_map = {}
    for s1 in stops:
        s1_id = s1['stop_id']
        s1_lat = float(s1['stop_lat'])
        s1_lon = float(s1['stop_lon'])
        nearby_map[s1_id] = []
        for s2 in stops:
            s2_id = s2['stop_id']
            if s1_id == s2_id:
                continue
            s2_lat = float(s2['stop_lat'])
            s2_lon = float(s2['stop_lon'])
            dist = haversine(s1_lat, s1_lon, s2_lat, s2_lon)
            if dist <= threshold:
                nearby_map[s1_id].append((s2_id, dist))
    return nearby_map
