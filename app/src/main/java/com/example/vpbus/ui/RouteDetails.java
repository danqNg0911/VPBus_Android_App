package com.example.vpbus.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusRoute;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.ui.BottomSheetPage.RDBottomSheetPageAdapter;
import com.example.vpbus.ui.fragments.RouteListStopsFragment;
import com.example.vpbus.util.DrawUtil;
import com.example.vpbus.util.MapMode;
import com.example.vpbus.util.NavigationUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteDetails extends AppCompatActivity {
    private PopupWindow popupWindow;
    private boolean isPopupVisible = false;
    private MapView mapView;
    private MapsManager mapsManager = new MapsManager();
    private RecyclerView recyclerViewStops;
    private StopAdapter stopAdapter;
    private AppDatabase db;
    private boolean isReversed = false;
    private List<BusStop> currentStops = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.map_route);
        String routeShortName = getIntent().getStringExtra("route_short_name");
        if (routeShortName == null) {
            // Nếu chẳng may chưa có, có thể log hoặc xử lý lỗi
            Log.e("RouteDetails", "Không nhận được route_id từ Intent!");
            return;
        }
        EdgeToEdge.enable(this);

        TextView routeName = findViewById(R.id.route_name);
        TextView firstDir = findViewById(R.id.first_dir);
        TextView secondDir = findViewById(R.id.second_dir);


        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(view ->{
            NavigationUtil.goTo(this, RouteActivity.class);
        });

        Bundle args = new Bundle();
        args.putString(MapMode.ARG_MODE, MapMode.MODE_ROUTE_DETAILS); // hoặc MODE_DIRECTION, ...


        ImageButton infoButton = findViewById(R.id.info);
        infoButton.setOnClickListener(view ->{
            if (isPopupVisible && popupWindow != null) {
                popupWindow.dismiss();
                isPopupVisible = false;
            } else {
                // Inflate layout popup_info.xml
                View popupView = LayoutInflater.from(this).inflate(R.layout.popup_info, null);

                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                );

    //            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_background));

                int[] location = new int[2];
                infoButton.getLocationOnScreen(location);
                int anchorX = location[0];
                int anchorY = location[1];

                popupWindow.showAtLocation(infoButton, Gravity.NO_GRAVITY, anchorX, anchorY - 120); // chỉnh -20 để dịch lên một chút

                isPopupVisible = true;

                // Tự động ẩn nếu người dùng bấm ra ngoài
                popupWindow.setOutsideTouchable(true);
                popupWindow.setOnDismissListener(() -> isPopupVisible = false);
            }
        });

        mapView = findViewById(R.id.mapView);

        try {
            mapsManager.initMap(this, mapView, "vinhphuc_v5.map", "Elevate2.xml");
            mapsManager.setInitialPosition(mapView, 21.0278, 105.8342, (byte)12);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppDatabase db = AppDatabase.getInstance(this);

        // Lấy thông tin tuyến
        new Thread(() -> {
            BusRoute route = db.busRouteDao().getRouteByShortName(routeShortName);

            List<String> tripIds = db.tripDao().getTripIdsByShortName(routeShortName);
            if (tripIds == null || tripIds.isEmpty()) return;
            String tripId = tripIds.get(0);

            List<BusStop> stops = db.busStopsDao().getStopsByTrip(tripId);
            if (stops == null || stops.isEmpty()) return;

            BusStop firstStop = stops.get(0);
            BusStop lastStop = stops.get(stops.size() - 1);

            runOnUiThread(() -> {
                if (route != null)
                    routeName.setText(route.getLong_name()); // "VP01 : Bồ Sao - Mê Linh Plaza"

                firstDir.setText("Chiều đi: " + firstStop.getStop_name());
                secondDir.setText("Chiều về: " + lastStop.getStop_name());
            });
        }).start();

        ImageView imageView = findViewById(R.id.busIcon);
        Bitmap icon = DrawUtil.createBusIcon(this, routeShortName);
        imageView.setImageBitmap(icon);

        ImageButton changeDirectionBtn = findViewById(R.id.change_direct);
        changeDirectionBtn.setOnClickListener(v -> {
            isReversed = !isReversed;

            CharSequence temp = firstDir.getText();
            firstDir.setText(secondDir.getText());
            secondDir.setText(temp);

            // Gửi tín hiệu cho fragment hiện tại (Trạm dừng)
            RouteListStopsFragment fragment =
                    (RouteListStopsFragment) getSupportFragmentManager()
                            .findFragmentByTag("f1"); // tab thứ 1 trong ViewPager2
            if (fragment != null) {
                fragment.reverseStops();
            }
        });

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        ImageView arrowPanel = findViewById(R.id.arrow_panel);

        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setDraggable(false);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        arrowPanel.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                arrowPanel.setImageResource(R.drawable.outline_keyboard_double_arrow_up_24);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                arrowPanel.setImageResource(R.drawable.baseline_keyboard_double_arrow_down_24);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        RDBottomSheetPageAdapter adapter = new RDBottomSheetPageAdapter(this, routeShortName);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Biểu đồ giờ");
                    break;
                case 1:
                    tab.setText("Trạm dừng");
                    break;
            }
        }).attach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsManager.onDestroy(mapView);
    }

}
