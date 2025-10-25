import osmnx as ox
import sqlite3

print(ox.__version__)
ox.settings.overpass_endpoint = "http://overpass.kumi.systems/api/interpreter"
ox.settings.timeout = 180  # tăng thời gian timeout nếu mạng chậm
ox.settings.use_cache = False
ox.settings.log_console = True

# Bounding box của tỉnh Vĩnh Phúc
north = 21.5317
south = 21.1382
east = 105.6712
west = 105.2203

bbox = (north, south, east, west) 
# Tạo graph với network_type="walk"
print("Dang tai graph di bo tu OSM...")

place = "Vinh Phuc Province, Vietnam"
G = ox.graph.graph_from_place(place, network_type="walk", simplify=True)
# print("Nodes:", len(G.nodes))
# print("Edges:", len(G.edges))

# Tạo database SQLite
db_file = "walk_graph.db"
conn = sqlite3.connect(db_file)
cur = conn.cursor()

# Xóa bảng cũ (nếu có)
cur.execute("DROP TABLE IF EXISTS nodes;")
cur.execute("DROP TABLE IF EXISTS edges;")

# Tạo bảng nodes
cur.execute("""
CREATE TABLE nodes (
    id INTEGER PRIMARY KEY,
    lat REAL,
    lng REAL
);
""")

# Tạo bảng edges
cur.execute("""
CREATE TABLE edges (
    from_id INTEGER,
    to_id INTEGER,
    distance REAL
);
""")

# Ghi dữ liệu nodes
print(f" Dang ghi {len(G.nodes)} nodes...")
for node_id, data in G.nodes(data=True):
    cur.execute(
        "INSERT INTO nodes (id, lat, lng) VALUES (?, ?, ?)",
        (node_id, data['y'], data['x'])
    )

# Ghi dữ liệu edges
print(f" Dang ghi {len(G.edges)} edges...")
for u, v, data in G.edges(data=True):
    distance = data.get('length', 1.0)  # fallback = 1m nếu thiếu
    cur.execute(
        "INSERT INTO edges (from_id, to_id, distance) VALUES (?, ?, ?)",
        (u, v, distance)
    )

conn.commit()
conn.close()
print(f" Da tao xong file database: {db_file}")
