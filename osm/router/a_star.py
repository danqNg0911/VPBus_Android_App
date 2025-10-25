import heapq
import math
from typing import Dict, Tuple, List, Any


def haversine(lat1, lon1, lat2, lon2):
    """Tính khoảng cách giữa 2 tọa độ (mét)"""
    R = 6371000
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)

    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlambda/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c


def build_graph(edges: List[Dict]) -> Dict[int, List[Tuple[int, float]]]:
    """Tạo graph dạng adjacency list từ bảng edges"""
    graph = {}
    for edge in edges:
        from_id = edge["from_id"]
        to_id = edge["to_id"]
        dist = edge["distance"]
        if from_id not in graph:
            graph[from_id] = []
        graph[from_id].append((to_id, dist))

        # Nếu 2 chiều: thêm chiều ngược
        if to_id not in graph:
            graph[to_id] = []
        graph[to_id].append((from_id, dist))
    return graph


def a_star_search(
    graph: Dict[int, List[Tuple[int, float]]],
    nodes: Dict[int, Tuple[float, float]],
    start: int,
    goal: int
) -> Tuple[float, List[int]]:
    """
    Chạy A* từ start node đến goal node
    Trả về: (tổng khoảng cách, danh sách node đi qua)
    """
    open_set = [(0, start)]
    came_from = {}
    g_score = {start: 0}

    def heuristic(n1, n2):
        lat1, lon1 = nodes[n1]
        lat2, lon2 = nodes[n2]
        return haversine(lat1, lon1, lat2, lon2)

    while open_set:
        _, current = heapq.heappop(open_set)

        if current == goal:
            path = []
            while current in came_from:
                path.append(current)
                current = came_from[current]
            path.append(start)
            path.reverse()
            print(f"[LOG] A* path found from {start} to {goal}, length={len(path)}")
            return g_score[goal], path

        for neighbor, dist in graph.get(current, []):
            tentative_g = g_score[current] + dist
            if neighbor not in g_score or tentative_g < g_score[neighbor]:
                came_from[neighbor] = current
                g_score[neighbor] = tentative_g
                f_score = tentative_g + heuristic(neighbor, goal)
                heapq.heappush(open_set, (f_score, neighbor))

    print(f"[LOG] A* no path from {start} to {goal}")
    return float("inf"), []
