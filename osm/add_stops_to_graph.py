import csv
import sqlite3
from geopy.distance import geodesic

DB_PATH = "walk_graph.db"  # đường dẫn đến file SQLite của bạn
STOPS_CSV = "stops.csv"  # đường dẫn đến stops.csv từ GTFS
NODE_ID_OFFSET = 14_000_000_000
TOLERANCE = 0.00005  # ~5m
MAX_WALK_DISTANCE = 100  # mét, để nối cạnh walk vào node gần nhất

def haversine_distance(lat1, lng1, lat2, lng2):
    return geodesic((lat1, lng1), (lat2, lng2)).meters

def get_nearest_node(cur, lat, lng, exclude_id=None, max_distance=MAX_WALK_DISTANCE):
    cur.execute("""
        SELECT id, lat, lng FROM nodes
        WHERE ABS(lat - ?) < 0.01 AND ABS(lng - ?) < 0.01
    """, (lat, lng))

    nearest = None
    min_dist = float('inf')

    for node_id, node_lat, node_lng in cur.fetchall():
        if exclude_id is not None and node_id == exclude_id:
            continue  # Bỏ qua chính node đang xét

        dist = haversine_distance(lat, lng, node_lat, node_lng)
        if dist < min_dist and dist <= max_distance:
            min_dist = dist
            nearest = (node_id, dist)

    return nearest


def main():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    # Tạo bảng stop_node_map nếu chưa có
    cur.execute("""
        CREATE TABLE IF NOT EXISTS stop_node_map (
            stop_id TEXT PRIMARY KEY,
            node_id INTEGER
        )
    """)

    # Mở file stops.csv
    with open(STOPS_CSV, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        diff = 0
        for row in reader:
            stop_id = row['\ufeffstop_id']
            lat = float(row['stop_lat'])
            lng = float(row['stop_lon'])

            # Kiểm tra tồn tại trong nodes chưa
            cur.execute("""
                SELECT id FROM nodes
                WHERE ABS(lat - ?) < ? AND ABS(lng - ?) < ?
                LIMIT 1
            """, (lat, TOLERANCE, lng, TOLERANCE))

            existing = cur.fetchone()
            if existing:
                node_id = existing[0]
                print(f"[=] Stop {stop_id} dung node san co: {node_id}")
            else:
                node_id = NODE_ID_OFFSET + diff
                cur.execute("INSERT INTO nodes (id, lat, lng) VALUES (?, ?, ?)", (node_id, lat, lng))
                print(f"them stop {stop_id} vao nodes voi id: {node_id}")
                diff += 1

            # Ghi map
            cur.execute("INSERT OR REPLACE INTO stop_node_map (stop_id, node_id) VALUES (?, ?)", (stop_id, node_id))

            # Tìm node OSM gần nhất để nối cạnh (walk)
            nearest = get_nearest_node(cur, lat, lng, exclude_id=node_id)
            if nearest:
                nearest_id, dist = nearest
                cur.execute("INSERT INTO edges (from_id, to_id, distance) VALUES (?, ?, ?)", (node_id, nearest_id, dist))
                cur.execute("INSERT INTO edges (from_id, to_id, distance) VALUES (?, ?, ?)", (nearest_id, node_id, dist))
                print(f"Noi stop {node_id} <-> node {nearest_id} voi khoang cach {int(dist)}m")

    conn.commit()
    conn.close()
    print("Hoan tat.")

if __name__ == "__main__":
    main()
