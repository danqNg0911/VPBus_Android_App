from collections import defaultdict
from datetime import datetime, timedelta
from typing import List, Dict, Tuple, Any


def parse_time(time_str: str) -> int:
    """Chuyển 'HH:MM:SS' thành số giây từ 0h (dễ so sánh)"""
    h, m, s = map(int, time_str.split(":"))
    return h * 3600 + m * 60 + s


def run_raptor(
    trips: List[Dict],
    stop_times: List[Dict],
    stop_node_map: Dict[str, int],
    from_stops: List[str],
    to_stops: List[str],
    start_time: datetime,
    max_transfers: int = 6,
    nearby_stop_map: Dict[str, List[Tuple[str, float]]] = {},
    walking_threshold: float = 500.0 #mét
) -> List[Dict]:
    """
    Chạy RAPTOR từ from_stops đến to_stops
    Dựa trên stop_times và trips (đã load từ DB)
    """
    print(f"[LOG] ==== RAPTOR start at {start_time.strftime('%H:%M:%S')} ====")
    start_seconds = start_time.hour * 3600 + start_time.minute * 60 + start_time.second

    # Tiền xử lý: nhóm stop_times theo trip
    stop_times_by_trip = defaultdict(list)
    for st in stop_times:
        stop_times_by_trip[st["trip_id"]].append(st)

    # Tiền xử lý: map trip_id → route_id
    trip_to_route = {trip["trip_id"]: trip["route_id"] for trip in trips}

    # Map stop_id → thời gian đến sớm nhất (tính bằng giây)
    earliest_arrival = defaultdict(lambda: float("inf"))

    # Lượt 0: đánh dấu các bến xuất phát
    marked_stops = set(from_stops)
    journeys = []

    for round_num in range(max_transfers + 1):
        print(f"[LOG] Round {round_num} — marked stops: {len(marked_stops)}")

        new_marked = set()

        for trip_id, st_list in stop_times_by_trip.items():
            # Sắp xếp stop_times theo stop_sequence để đảm bảo thứ tự
            st_list = sorted(st_list, key=lambda s: int(s["stop_sequence"]))

            boarding_index = None
            for i, st in enumerate(st_list):
                stop_id = st["stop_id"]

                # Nếu chưa lên xe và đây là bến được đánh dấu
                if boarding_index is None and stop_id in marked_stops:
                    depart_time = parse_time(st["departure_time"])
                    if depart_time >= start_seconds:
                        boarding_index = i  # Bắt đầu chuyến đi tại đây

                # Nếu đã lên xe, duyệt các bến tiếp theo để mở rộng
                elif boarding_index is not None and i > boarding_index:
                    prev_st = st_list[boarding_index]
                    arrive_time = parse_time(st["arrival_time"])
                    target_stop_id = st["stop_id"]

                    # Nếu tìm được đường đi tốt hơn
                    if arrive_time < earliest_arrival[target_stop_id]:
                        earliest_arrival[target_stop_id] = arrive_time
                        new_marked.add(target_stop_id)

                        journey = {
                            "trip_id": trip_id,
                            "from_stop": prev_st["stop_id"],
                            "to_stop": target_stop_id,
                            "depart_time": prev_st["departure_time"],
                            "arrive_time": st["arrival_time"],
                            "route_id": trip_to_route[trip_id],
                            "round": round_num
                        }
                        journeys.append(journey)
                        #print(f"[DEBUG] Đi {journey['from_stop']} → {journey['to_stop']} trên tuyến {journey['route_id']} chuyến {trip_id} lúc {journey['depart_time']} → {journey['arrive_time']}")

        # tìm dường đi bộ giao 2 tuyến
        for stop in list(new_marked):
            for nearby_stop, dist in nearby_stop_map.get(stop, []):
                if dist < walking_threshold:
                    walk_time_min = dist / 80  # phút
                    new_arrival_time = earliest_arrival[stop] + walk_time_min
                    if new_arrival_time < earliest_arrival[nearby_stop]:
                        earliest_arrival[nearby_stop] = new_arrival_time
                        marked_stops.add(nearby_stop)
                        new_marked.add(nearby_stop)
                    #print(f"[WALK] Added walking connection: {stop} → {nearby_stop}, dist={dist}m")


        if not new_marked:
            print("[LOG] Không còn bến nào mới → kết thúc RAPTOR")
            break

        marked_stops = new_marked

    # Chỉ lọc các chuyến kết thúc ở bến mong muốn
    # Lọc: chỉ lấy journey bắt đầu từ from_stops và kết thúc ở to_stops
    result = [
        j for j in journeys
        if j["to_stop"] in to_stops
    ]


    print(f"[LOG] ==== RAPTOR DONE → {len(result)} journey(s) đến đích ====")
    return result
