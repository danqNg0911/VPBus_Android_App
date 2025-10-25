import math
from typing import List, Dict


def haversine(lat1, lon1, lat2, lon2):
    """Tính khoảng cách giữa 2 tọa độ (đơn vị: mét)"""
    R = 6371000  # Bán kính Trái Đất (m)
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)

    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlambda/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c


def find_nearest_stops(
    lat: float,
    lng: float,
    stops: List[Dict],
    radius: float = 500.0
) -> List[Dict]:
    """
    Tìm tất cả các bến trong bán kính 'radius' mét từ vị trí (lat, lng)
    Trả về danh sách stop dict có thêm trường 'distance'
    """
    nearby = []
    for stop in stops:
        stop_lat = float(stop["stop_lat"])
        stop_lon = float(stop["stop_lon"])
        dist = haversine(lat, lng, stop_lat, stop_lon)
        if dist <= radius:
            stop_with_dist = dict(stop)
            stop_with_dist["distance"] = dist
            nearby.append(stop_with_dist)

    # Sắp xếp theo khoảng cách tăng dần
    nearby.sort(key=lambda s: s["distance"])

    print(f"[LOG] Found {len(nearby)} stops within {radius}m of ({lat}, {lng})")
    if len(nearby) > 0:
        print(f"[LOG] Nearest stop: {nearby[0]['stop_name']} ({nearby[0]['distance']:.1f}m)")

    return nearby
