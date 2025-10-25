package com.example.vpbus.ui;

import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vpbus.R;
import com.example.vpbus.service.LocationService;
import com.example.vpbus.service.MapService;
import com.example.vpbus.ui.BottomSheetPage.SPBottomSheetPageAdapter;
import com.example.vpbus.util.NavigationUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import java.io.IOException;

public class StopsActivity extends AppCompatActivity {

    private MapView mapStopView;
    private MapsManager mapsManager = new MapsManager();
    private MapService mapService = new MapService();
    private Location lastKnownLocation;
    private LocationService locationService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.map_stops);
        EdgeToEdge.enable(this);

        Button suggest = findViewById(R.id.suggestButton);
        suggest.setOnClickListener(view -> {
            NavigationUtil.goTo(this, Main.class);
        });

        Button route = findViewById(R.id.routeButton);
        route.setOnClickListener(view -> {
            NavigationUtil.goTo(this, RouteActivity.class);
        });


        mapStopView = findViewById(R.id.mapView);

        locationService = new LocationService(this);

        try {
            mapsManager.initMap(this, mapStopView, "vinhphuc_v5.map", "Elevate2.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationService.requestLocationUpdates(location -> {
            lastKnownLocation = location;
            mapService.showCurrentLocation(location, mapStopView, StopsActivity.this);

            // lần đầu tiên có location thì lia map luôn
            if (lastKnownLocation != null) {
                mapService.centerMapOnce(lastKnownLocation, mapStopView, (byte) 15);
            }
        });

        ImageButton btnRecenter = findViewById(R.id.buttonRecenter);
        btnRecenter.setOnClickListener(v -> {
            mapService.recenterToCurrentLocation(lastKnownLocation, mapStopView);
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        SPBottomSheetPageAdapter adapter = new SPBottomSheetPageAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Các bến gần đây"); break;
                        case 1: tab.setText("Các bến yêu thích"); break;
                    }
                }
        ).attach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsManager.onDestroy(mapStopView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationService.handlePermissionResult(
                requestCode,
                grantResults,
                () -> {
                    // granted
                    locationService.requestLocationUpdates(location -> {
                        lastKnownLocation = location;
                        mapService.showCurrentLocation(location, mapStopView, this);
                        mapService.centerMapOnce(location, mapStopView, (byte) 13);
                    });
                    locationService.getLastKnownLocation(location -> {
                        lastKnownLocation = location;
                        mapService.showCurrentLocation(location, mapStopView, this);
                        mapService.centerMapOnce(location, mapStopView, (byte) 13);
                    });
                },
                () -> Toast.makeText(this, "Ứng dụng cần quyền GPS để hiển thị vị trí", Toast.LENGTH_SHORT).show()
        );
    }

}


