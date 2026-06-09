# VPBus / PTBus

Ứng dụng Android Java native hỗ trợ tra cứu và gợi ý di chuyển bằng xe bus trên địa bàn Vĩnh Phúc. Project dùng dữ liệu bus, graph đi bộ và bản đồ offline trong asset để hiển thị tuyến, bến, vị trí người dùng và các phương án hành trình.

## Tính năng chính

- Chỉ đường bằng xe bus:
  - Chọn điểm đi và điểm đến trực tiếp trên bản đồ.
  - Gợi ý các phương án di chuyển bằng thuật toán transit routing.
  - Xem chi tiết hành trình trên bản đồ, gồm các leg đi bộ và đi bus.
- Xem bến:
  - Hiển thị các bến bus gần vị trí hiện tại.
  - Danh sách bến gần đây trong bán kính 2km.
  - Xem chi tiết bến và lưu bến yêu thích bằng local storage.
- Xem tuyến:
  - Danh sách tuyến bus.
  - Xem chi tiết tuyến trên bản đồ.
  - Xem danh sách trạm dừng, biểu đồ/khung giờ và đổi chiều tuyến.
- Bản đồ:
  - Dùng Mapsforge với file map offline `vinhphuc_v5.map`.
  - Hiển thị marker vị trí hiện tại, bến, highlight stop và mũi tên chiều tuyến.

## Công nghệ

- Ngôn ngữ: Java
- Nền tảng: Android native
- Build system: Gradle Kotlin DSL
- Android Gradle Plugin: 8.11.0
- Java compatibility: Java 11
- Min SDK: 24
- Target/Compile SDK: 36
- Database: Room + SQLite asset
- Map: Mapsforge
- UI chính: AppCompat, Material Components, ConstraintLayout, RecyclerView, ViewPager2
- Location: Android LocationManager / Google Play Services Location dependency

## Cấu trúc project

Project hiện có một module Android application:

```text
VPBus/
├── app/
│   ├── src/main/java/com/example/vpbus/
│   │   ├── data/          # Room database, DAO, local repositories
│   │   ├── model/         # Entity/model/DTO
│   │   ├── service/       # Location, map service
│   │   ├── service/Router # A*, RAPTOR, transit planner
│   │   ├── ui/            # Activity, adapter, fragment, map manager
│   │   └── util/          # Helper/navigation/drawing utilities
│   ├── src/main/assets/
│   │   ├── database/walk_graph.db
│   │   ├── vinhphuc_v5.map
│   │   └── Elevate*.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts
```

Ngoài module Android, thư mục `osm/` chứa script và dữ liệu phục vụ xử lý OSM/GTFS, không phải Gradle module.

## Dữ liệu và thuật toán

Ứng dụng lấy dữ liệu chính từ SQLite asset `app/src/main/assets/database/walk_graph.db`, được Room load qua `AppDatabase`.

Các bảng/dữ liệu quan trọng gồm:

- `stops`: danh sách bến bus.
- `routes`, `trips`, `stop_times`, `shapes`: dữ liệu GTFS/tuyến bus.
- `nodes`, `edges`, `stop_node_map`: graph đi bộ và mapping bến với node.

Routing hiện dùng:

- `NearestStopFinder`: tìm bến gần điểm đi/đến và bến chuyển tuyến.
- `RaptorAlg`: tìm chuỗi leg bus theo thời gian.
- `AStarAlg`: tìm đường đi bộ trên graph giữa vị trí/bến/node.
- `TransitPlanner`: kết hợp dữ liệu, chạy RAPTOR, dựng hành trình đầy đủ, sort/deduplicate kết quả.

## Các màn hình chính

- `Main`: màn hình chính của tab Chỉ đường.
- `CheckSearch`: chọn điểm đi/đến trên bản đồ.
- `SuggestRoute`: danh sách phương án di chuyển.
- `JourneyMapActivity`: bản đồ chi tiết của một phương án hành trình.
- `StopsActivity`: page Xem bến.
- `BusStopDetailActivity`: chi tiết bến và yêu thích.
- `RouteActivity`: danh sách tuyến.
- `RouteDetails`: chi tiết tuyến, bản đồ tuyến, danh sách trạm dừng và lịch.

## Cấu hình cần có

Máy phát triển cần:

- JDK phù hợp với Android Gradle Plugin, hiện project đang build được với JDK 21.
- Android SDK.
- Android Platform Tools (`adb`).
- Android Emulator hoặc thiết bị Android thật.

File `local.properties` cần có đường dẫn Android SDK:

```properties
sdk.dir=C\:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
```

Project cũng đọc `MAPS_API_KEY` từ `local.properties` để inject vào manifest:

```properties
MAPS_API_KEY=your_google_maps_api_key
```

Lưu ý: phần bản đồ chính hiện dùng Mapsforge offline. Google Maps API key vẫn đang được khai báo vì project có dependency Google Maps/metadata manifest.

## Build và chạy

Từ root project:

```powershell
cd F:\Project\VPBus\VPBus
```

Kiểm tra thiết bị/emulator:

```powershell
adb devices
```

Build debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Cài app debug lên thiết bị/emulator:

```powershell
.\gradlew.bat installDebug
```

APK debug sau khi build nằm tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Chạy bằng VS Code

VS Code có thể dùng để sửa code, build và install app qua terminal. Nên cài:

- Extension Pack for Java
- Gradle for Java
- XML
- Android iOS Emulator, nếu muốn mở emulator từ VS Code

Các lệnh build/run vẫn dùng Gradle wrapper:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Android Studio vẫn tiện hơn cho emulator manager, layout preview, logcat và debug Android chuyên sâu.

## Kiểm thử

Chạy unit/instrumented test nếu cần:

```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

Kiểm tra build nhanh:

```powershell
.\gradlew.bat assembleDebug --console=plain
```

## Lưu ý hiện trạng

- Dữ liệu POI/địa điểm yêu thích theo tên chưa được triển khai; hiện mới có cơ chế tab/placeholder cho khu vực cá nhân.
- Bến yêu thích được lưu local bằng `SharedPreferences`.
- Routing phụ thuộc chất lượng dữ liệu GTFS, graph đi bộ và mapping `stop_node_map`.
- Khi test chỉ đường, nên chọn điểm gần khu vực có bến để thuật toán tìm được điểm lên/xuống phù hợp.
