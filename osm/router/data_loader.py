import sqlite3
from typing import Dict, List, Any

def load_data_from_db(db_path: str) -> Dict[str, List[Dict[str, Any]]]:
    """Tải toàn bộ dữ liệu cần thiết từ DB SQLite."""
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row  # Trả về dict-like rows
    cur = conn.cursor()

    data = {}

    # Load stops
    cur.execute("SELECT * FROM stops")
    data["stops"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['stops'])} stops")

    # Load stop_node_map
    cur.execute("SELECT * FROM stop_node_map")
    data["stop_node_map"] = {row["stop_id"]: row["node_id"] for row in cur.fetchall()}
    print(f"[LOG] Loaded {len(data['stop_node_map'])} stop_node mappings")

    # Load nodes
    cur.execute("SELECT * FROM nodes")
    data["nodes"] = {row["id"]: (row["lat"], row["lng"]) for row in cur.fetchall()}
    print(f"[LOG] Loaded {len(data['nodes'])} nodes")

    # Load edges
    cur.execute("SELECT * FROM edges")
    data["edges"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['edges'])} edges")

    # Load routes
    cur.execute("SELECT * FROM routes")
    data["routes"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['routes'])} routes")

    # Load trips
    cur.execute("SELECT * FROM trips")
    data["trips"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['trips'])} trips")

    # Load stop_times
    cur.execute("SELECT * FROM stop_times")
    data["stop_times"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['stop_times'])} stop_times")

    # Load calendar
    cur.execute("SELECT * FROM calendar")
    data["calendar"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['calendar'])} calendar entries")

    # Load shapes
    cur.execute("SELECT * FROM shapes")
    data["shapes"] = [dict(row) for row in cur.fetchall()]
    print(f"[LOG] Loaded {len(data['shapes'])} shape points")

    conn.close()
    return data
