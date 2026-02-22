package com.example.vpbus.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.Journey;
import com.example.vpbus.model.JourneyLeg;
import com.example.vpbus.service.Router.TransitPlanner;
import com.example.vpbus.ui.Adapters.JourneyAdapter;
import com.example.vpbus.util.NavigationUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuggestRoute extends AppCompatActivity {

    private static final String TAG = "SuggestRoute_Test";
    private RecyclerView recyclerView;
    private JourneyAdapter adapter;
    private List<Journey> journeyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_trip);

        Button back = findViewById(R.id.backButton);

        back.setOnClickListener(view ->
                NavigationUtil.goTo(this, CheckSearch.class)
        );

        double beginLat = getIntent().getDoubleExtra("begin_lat", 0);
        double beginLng = getIntent().getDoubleExtra("begin_lng", 0);
        double endLat = getIntent().getDoubleExtra("end_lat", 0);
        double endLng = getIntent().getDoubleExtra("end_lng", 0);

        Button text1 = findViewById(R.id.editTextText);
        text1.setText(String.format("Điểm đón: %.5f, %.5f", beginLat, beginLng));

        Button text2 = findViewById(R.id.editTextText2);
        text2.setText(String.format("Điểm xuống: %.5f, %.5f", endLat, endLng));

        recyclerView = findViewById(R.id.rvSuggestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JourneyAdapter(this, journeyList);
        recyclerView.setAdapter(adapter);

        testRouting(beginLat, beginLng, endLat, endLng);

        runOnUiThread(()-> {
            journeyList.clear();
            journeyList.addAll(journeyList);
            adapter.notifyDataSetChanged();
        });
    }

    private void testRouting(double latFrom, double lngFrom, double latTo, double lngTo) {
        // Sử dụng Executor để không làm treo UI
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "Bắt đầu định tuyến...");

                // Khởi tạo Database và Planner
                AppDatabase db = AppDatabase.getInstance(this);
                TransitPlanner planner = new TransitPlanner(db);

                // Thiết lập thời gian giả định (08:00:00) như main.py
                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, 8);
                startTime.set(Calendar.MINUTE, 0);
                startTime.set(Calendar.SECOND, 0);

                // Gọi định tuyến
                List<Journey> journeyOptions = planner.planRoute(latFrom, lngFrom, latTo, lngTo, startTime);

                // 3. In kết quả ra Logcat (Tương ứng với main.py)
                Log.d(TAG, "==== KẾT QUẢ ĐỊNH TUYẾN ====");
                if (journeyOptions.isEmpty()) {
                    Log.w(TAG, "Không tìm thấy phương án hành trình nào.");
                }
                // CẬP NHẬT LÊN MÀN HÌNH TẠI ĐÂY
                runOnUiThread(() -> {
                    if (journeyOptions != null && !journeyOptions.isEmpty()) {
                        // Xóa dữ liệu cũ và thêm dữ liệu mới
                        this.journeyList.clear();
                        this.journeyList.addAll(journeyOptions);

                        // Thông báo cho adapter vẽ lại màn hình
                        this.adapter.notifyDataSetChanged();

                        Log.d(TAG, "Đã cập nhật " + journeyOptions.size() + " hành trình lên UI");
                    } else {
                        Toast.makeText(this, "Không tìm thấy lộ trình phù hợp", Toast.LENGTH_SHORT).show();
                    }
                });

                for (int i = 0; i < journeyOptions.size(); i++) {
                    Log.d(TAG, "\nPhương án " + (i + 1) + ":");
                    for (JourneyLeg leg : journeyOptions.get(i).legs) {
                        if (leg.getType().equals("walk")) {
                            Log.d(TAG, String.format(" -> [ĐI BỘ] Khoảng cách: %.1fm", leg.getDistance()));
                        } else if (leg.getType().equals("bus")) {
                            // busInfo là Map<String, Object> từ Raptor trả về
                            String routeId = (String) leg.getBusInfo().get("route_id");
                            String from = (String) leg.getBusInfo().get("from_stop");
                            String to = (String) leg.getBusInfo().get("to_stop");
                            String dep = (String) leg.getBusInfo().get("depart_time");
                            String arr = (String) leg.getBusInfo().get("arrive_time");

                            Log.d(TAG, String.format(" -> [BUS] Tuyến %s: %s (%s) -> %s (%s)",
                                    routeId, from, dep, to, arr));
                        }
                    }
                }
                Log.d(TAG, "============================");

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi chạy thuật toán: " + e.getMessage(), e);
            }
        });
    }
}
