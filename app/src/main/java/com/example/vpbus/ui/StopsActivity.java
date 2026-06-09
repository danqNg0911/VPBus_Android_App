package com.example.vpbus.ui;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.data.FavoriteStopsRepository;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.service.LocationService;
import com.example.vpbus.service.MapService;
import com.example.vpbus.service.Router.NearestStopFinder;
import com.example.vpbus.ui.BottomSheetPage.SPBottomSheetPageAdapter;
import com.example.vpbus.ui.fragments.StopsFavouriteFragment;
import com.example.vpbus.ui.fragments.StopsNearbyFragment;
import com.example.vpbus.util.NavigationUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StopsActivity extends AppCompatActivity implements StopsDataProvider {
    private static final double NEARBY_RADIUS_METERS = 2000.0;

    private MapView mapStopView;
    private final MapsManager mapsManager = new MapsManager();
    private final MapService mapService = new MapService();
    private Location lastKnownLocation;
    private LocationService locationService;
    private AppDatabase db;
    private FavoriteStopsRepository favoriteStopsRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<BusStop> allStops = new ArrayList<>();
    private final List<BusStop> nearbyStops = new ArrayList<>();
    private boolean stopsLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.map_stops);
        EdgeToEdge.enable(this);

        db = AppDatabase.getInstance(this);
        favoriteStopsRepository = new FavoriteStopsRepository(this);

        Button suggest = findViewById(R.id.suggestButton);
        suggest.setOnClickListener(view -> NavigationUtil.goTo(this, Main.class));

        Button route = findViewById(R.id.routeButton);
        route.setOnClickListener(view -> NavigationUtil.goTo(this, RouteActivity.class));

        mapStopView = findViewById(R.id.mapView);
        locationService = new LocationService(this);

        try {
            mapsManager.initMap(this, mapStopView, "vinhphuc_v5.map", "Elevate2.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupTabs();
        loadAllStops();
        requestLocation();

        ImageButton btnRecenter = findViewById(R.id.buttonRecenter);
        btnRecenter.setOnClickListener(v ->
                mapService.recenterToCurrentLocation(lastKnownLocation, mapStopView, StopsActivity.this)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStopFragments();
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        SPBottomSheetPageAdapter adapter = new SPBottomSheetPageAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("G\u1ea7n \u0111\u00e2y");
                            break;
                        case 1:
                            tab.setText("Y\u00eau th\u00edch");
                            break;
                    }
                }
        ).attach();
    }

    private void loadAllStops() {
        executorService.execute(() -> {
            List<BusStop> stops = db.busStopsDao().getAllStops();
            mainHandler.post(() -> {
                allStops.clear();
                if (stops != null) {
                    allStops.addAll(stops);
                }
                stopsLoaded = true;
                if (lastKnownLocation != null) {
                    updateNearbyStops(lastKnownLocation);
                }
                refreshStopFragments();
            });
        });
    }

    private void requestLocation() {
        locationService.requestLocationUpdates(location -> {
            lastKnownLocation = location;
            mapService.showCurrentLocation(location, mapStopView, StopsActivity.this);
            mapService.centerMapOnce(lastKnownLocation, mapStopView, (byte) 15);
            if (stopsLoaded) {
                updateNearbyStops(location);
            }
        });

        locationService.getLastKnownLocation(location -> {
            lastKnownLocation = location;
            mapService.showCurrentLocation(location, mapStopView, StopsActivity.this);
            mapService.centerMapOnce(location, mapStopView, (byte) 15);
            if (stopsLoaded) {
                updateNearbyStops(location);
            }
        });
    }

    private void updateNearbyStops(Location location) {
        if (location == null) return;

        List<BusStop> filtered = new ArrayList<>();
        for (BusStop stop : allStops) {
            double distance = NearestStopFinder.haversine(
                    location.getLatitude(),
                    location.getLongitude(),
                    stop.getStop_lat(),
                    stop.getStop_lon()
            );
            if (distance <= NEARBY_RADIUS_METERS) {
                BusStop nearbyStop = new BusStop(
                        stop.getStop_id(),
                        stop.getStop_name(),
                        stop.getStop_lat(),
                        stop.getStop_lon()
                );
                nearbyStop.distance = distance;
                filtered.add(nearbyStop);
            }
        }
        filtered.sort(Comparator.comparingDouble(stop -> stop.distance));

        nearbyStops.clear();
        nearbyStops.addAll(filtered);
        mapsManager.drawBusStopsLazy(nearbyStops, mapStopView, this);
        refreshStopFragments();
    }

    private void refreshStopFragments() {
        Fragment nearbyFragment = getSupportFragmentManager().findFragmentByTag("f0");
        if (nearbyFragment instanceof StopsNearbyFragment) {
            ((StopsNearbyFragment) nearbyFragment).refresh();
        }

        Fragment favoriteFragment = getSupportFragmentManager().findFragmentByTag("f1");
        if (favoriteFragment instanceof StopsFavouriteFragment) {
            ((StopsFavouriteFragment) favoriteFragment).refresh();
        }
    }

    @Override
    public List<BusStop> getNearbyStops() {
        return new ArrayList<>(nearbyStops);
    }

    @Override
    public List<BusStop> getFavoriteStops() {
        Set<String> favoriteIds = favoriteStopsRepository.getFavoriteIds();
        if (favoriteIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BusStop> favorites = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        for (BusStop stop : nearbyStops) {
            if (favoriteIds.contains(stop.getStop_id())) {
                favorites.add(stop);
                addedIds.add(stop.getStop_id());
            }
        }

        for (BusStop stop : allStops) {
            if (favoriteIds.contains(stop.getStop_id()) && !addedIds.contains(stop.getStop_id())) {
                favorites.add(stop);
            }
        }

        favorites.sort(Comparator.comparing(BusStop::getStop_name));
        return favorites;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.stopLocationUpdates();
        executorService.shutdownNow();
        mapsManager.onDestroy(mapStopView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationService.handlePermissionResult(
                requestCode,
                grantResults,
                this::requestLocation,
                () -> Toast.makeText(this, "\u1ee8ng d\u1ee5ng c\u1ea7n quy\u1ec1n GPS \u0111\u1ec3 hi\u1ec3n th\u1ecb v\u1ecb tr\u00ed", Toast.LENGTH_SHORT).show()
        );
    }
}
