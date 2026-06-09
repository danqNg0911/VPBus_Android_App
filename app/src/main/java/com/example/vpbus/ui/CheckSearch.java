package com.example.vpbus.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import com.example.vpbus.R;
import com.example.vpbus.service.LocationService;
import com.example.vpbus.service.MapService;
import com.example.vpbus.util.NavigationUtil;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.IOException;
import java.util.Locale;

public class CheckSearch extends AppCompatActivity {
    private enum PickTarget {
        START,
        END
    }

    private MapView mapView;
    private final MapsManager mapsManager = new MapsManager();
    private LatLong beginLatLng = null;
    private LatLong endLatLng = null;
    private Marker beginMarker = null;
    private Marker endMarker = null;
    private Button beginPick;
    private Button endPick;
    private TextView pickInstruction;
    private PickTarget activeTarget = PickTarget.START;
    private Location lastKnownLocation;
    private LocationService locationService;
    private final MapService mapService = new MapService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.select_place);

        Button searchRoute = findViewById(R.id.searchRoute);
        Button back = findViewById(R.id.backButton);
        Button clearSelectionButton = findViewById(R.id.clearSelectionButton);
        beginPick = findViewById(R.id.beginPick);
        endPick = findViewById(R.id.endPick);
        pickInstruction = findViewById(R.id.pickInstruction);

        beginPick.setOnClickListener(view -> setActiveTarget(PickTarget.START));
        endPick.setOnClickListener(view -> setActiveTarget(PickTarget.END));
        clearSelectionButton.setOnClickListener(view -> clearSelection());

        searchRoute.setOnClickListener(view -> {
            if (beginLatLng == null || endLatLng == null) {
                Toast.makeText(this, "Vui l\u00f2ng ch\u1ecdn \u0111i\u1ec3m b\u1eaft \u0111\u1ea7u v\u00e0 k\u1ebft th\u00fac", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, SuggestRoute.class);
            intent.putExtra("begin_lat", beginLatLng.latitude);
            intent.putExtra("begin_lng", beginLatLng.longitude);
            intent.putExtra("end_lat", endLatLng.latitude);
            intent.putExtra("end_lng", endLatLng.longitude);

            startActivity(intent);
        });

        back.setOnClickListener(view -> NavigationUtil.goTo(this, Main.class));

        AndroidGraphicFactory.createInstance(getApplication());
        mapView = findViewById(R.id.mapView);

        try {
            mapsManager.initMap(this, mapView, "vinhphuc_v5.map", "Elevate2.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationService = new LocationService(this);
        locationService.requestLocationUpdates(location -> {
            lastKnownLocation = location;
            mapService.showCurrentLocation(location, mapView, CheckSearch.this);

            if (lastKnownLocation != null) {
                mapService.centerMapOnce(lastKnownLocation, mapView, (byte) 14);
            }
        });

        ImageButton btnRecenter = findViewById(R.id.buttonRecenter);
        if (btnRecenter != null) {
            btnRecenter.setOnClickListener(v -> mapService.recenterToCurrentLocation(lastKnownLocation, mapView, this));
        }

        mapView.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(event.getX() - downX);
                        float dy = Math.abs(event.getY() - downY);
                        if (dx < 10 && dy < 10) {
                            LatLong latLong = mapView.getMapViewProjection()
                                    .fromPixels((int) event.getX(), (int) event.getY());
                            handleTap(latLong);
                        }
                        break;
                }
                return false;
            }
        });

        setActiveTarget(PickTarget.START);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.stopLocationUpdates();
        mapsManager.onDestroy(mapView);
    }

    private void handleTap(LatLong latLong) {
        Log.d("CheckSearch", "User tap at: lat=" + latLong.latitude + ", lng=" + latLong.longitude);

        if (activeTarget == PickTarget.START) {
            beginLatLng = latLong;
            showBeginMarker(latLong);
            beginPick.setText(String.format(Locale.US, "\u0110i\u1ec3m \u0111\u00f3n: %.5f, %.5f", latLong.latitude, latLong.longitude));
            setActiveTarget(PickTarget.END);
            return;
        }

        endLatLng = latLong;
        showEndMarker(latLong);
        endPick.setText(String.format(Locale.US, "\u0110i\u1ec3m xu\u1ed1ng: %.5f, %.5f", latLong.latitude, latLong.longitude));
        updateInstruction();
    }

    private void setActiveTarget(PickTarget target) {
        activeTarget = target;
        beginPick.setTextColor(target == PickTarget.START ? Color.parseColor("#BB0000") : Color.BLACK);
        endPick.setTextColor(target == PickTarget.END ? Color.parseColor("#BB0000") : Color.BLACK);
        updateInstruction();
    }

    private void updateInstruction() {
        if (activeTarget == PickTarget.START) {
            pickInstruction.setText("Ch\u1ea1m map \u0111\u1ec3 ch\u1ecdn \u0111i\u1ec3m \u0111i");
        } else {
            pickInstruction.setText("Ch\u1ea1m map \u0111\u1ec3 ch\u1ecdn \u0111i\u1ec3m \u0111\u1ebfn");
        }
    }

    private void showBeginMarker(LatLong latLong) {
        if (beginMarker != null) {
            mapView.getLayerManager().getLayers().remove(beginMarker);
        }

        var bitmap = AndroidGraphicFactory.convertToBitmap(
                getResources().getDrawable(R.drawable.stop_point_2light, null)
        );

        beginMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(beginMarker);
        mapView.invalidate();
    }

    private void showEndMarker(LatLong latLong) {
        if (endMarker != null) {
            mapView.getLayerManager().getLayers().remove(endMarker);
        }

        var bitmap = AndroidGraphicFactory.convertToBitmap(
                getResources().getDrawable(R.drawable.stop_point_2light, null)
        );

        endMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(endMarker);
        mapView.invalidate();
    }

    private void clearSelection() {
        if (beginMarker != null) {
            mapView.getLayerManager().getLayers().remove(beginMarker);
        }
        if (endMarker != null) {
            mapView.getLayerManager().getLayers().remove(endMarker);
        }

        beginMarker = null;
        endMarker = null;
        beginLatLng = null;
        endLatLng = null;

        beginPick.setText("  \u0110i\u1ec3m \u0111i");
        endPick.setText("  \u0110i\u1ec3m \u0111\u1ebfn");
        setActiveTarget(PickTarget.START);
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationService.handlePermissionResult(
                requestCode,
                grantResults,
                () -> {
                    locationService.requestLocationUpdates(location -> {
                        lastKnownLocation = location;
                        mapService.showCurrentLocation(location, mapView, this);
                        mapService.centerMapOnce(location, mapView, (byte) 14);
                    });
                    locationService.getLastKnownLocation(location -> {
                        lastKnownLocation = location;
                        mapService.showCurrentLocation(location, mapView, this);
                        mapService.centerMapOnce(location, mapView, (byte) 14);
                    });
                },
                () -> Toast.makeText(this, "\u1ee8ng d\u1ee5ng c\u1ea7n quy\u1ec1n GPS \u0111\u1ec3 hi\u1ec3n th\u1ecb v\u1ecb tr\u00ed", Toast.LENGTH_SHORT).show()
        );
    }
}
