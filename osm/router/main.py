# main.py

from datetime import datetime
from planner import plan_route

if __name__ == "__main__":
    # Đường dẫn đến file SQLite
    db_path = "walk_graph.db"

    # Tọa độ điểm bắt đầu và kết thúc (lat, lng)
    lat_from = 21.2750493
    lng_from = 105.6491113

 
    lat_to = 21.28089647
    lng_to = 105.5405821

    # Thời gian bắt đầu (giờ hiện tại)
    start_time = datetime.strptime("08:00:00", "%H:%M:%S")

    # Gọi định tuyến
    journey_options = plan_route(db_path, lat_from, lng_from, lat_to, lng_to, start_time)

    # In ra kết quả
    print("\nCác phương án hành trình khả thi:")
    for idx, option in enumerate(journey_options, start=1):
        print(f"\nPhương án {idx}:")
        for leg in option['journeyLegs']:
            if leg['type'] == 'walk':
                print(f"Đi bộ từ node {leg['from_node']} đến node {leg['to_node']} ({leg['distance']:.1f}m)")
            elif leg['type'] == 'bus':
                print(f"Tuyến {leg['route_id']} từ bến {leg['from_stop']} đến {leg['to_stop']}")
                print(f"Lên lúc {leg['depart_time']} - Xuống lúc {leg['arrive_time']}")
