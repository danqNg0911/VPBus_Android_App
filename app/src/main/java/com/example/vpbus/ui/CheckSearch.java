package com.example.vpbus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import com.example.vpbus.R;
import com.example.vpbus.util.NavigationUtil;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.IOException;

public class CheckSearch extends AppCompatActivity {
    private MapView mapView;
    private MapsManager mapsManager = new MapsManager();
    private LatLong beginLatLng = null;
    private LatLong endLatLng = null;
    private Marker beginMarker = null;
    private Marker endMarker = null;
    Button beginPick;
    Button endPick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.select_place);

        Button searchRoute = findViewById(R.id.searchRoute);
        Button back = findViewById(R.id.backButton);
        beginPick = findViewById(R.id.beginPick);
        endPick = findViewById(R.id.endPick);

//        searchRoute.setOnClickListener(view ->
//                NavigationUtil.goTo(this, SuggestRoute.class)
//        );

        searchRoute.setOnClickListener(view -> {
            if (beginLatLng == null || endLatLng == null) {
                Toast.makeText(this, "Vui lòng chọn điểm bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, SuggestRoute.class);
            intent.putExtra("begin_lat", beginLatLng.latitude);
            intent.putExtra("begin_lng", beginLatLng.longitude);
            intent.putExtra("end_lat", endLatLng.latitude);
            intent.putExtra("end_lng", endLatLng.longitude);

            startActivity(intent);
        });


        back.setOnClickListener(view ->
                NavigationUtil.goTo(this, Main.class)
        );

        AndroidGraphicFactory.createInstance(getApplication());
        mapView = findViewById(R.id.mapView);

        try {
            mapsManager.initMap(this, mapView, "vinhphuc_v5.map", "Elevate2.xml");
            mapsManager.setInitialPosition(mapView, 21.21, 105.56, (byte)12);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapView.setOnTouchListener(new View.OnTouchListener() {

            private float downX, downY;

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsManager.onDestroy(mapView);
    }

    private void handleTap(LatLong latLong) {
        Log.d("CheckSearch", "User tap at: lat="
                + latLong.latitude + ", lng=" + latLong.longitude);

        if (beginLatLng == null) {
            beginLatLng = latLong;
            Log.d("CheckSearch", "BEGIN selected: " + beginLatLng.latitude + ", " + beginLatLng.longitude);
            showBeginMarker(latLong);
            beginPick.setText(
                    String.format("Điểm đón: %.5f, %.5f",
                            latLong.latitude, latLong.longitude)
            );
        } else if (endLatLng == null) {
            endLatLng = latLong;
            Log.d("CheckSearch", "END selected: " + endLatLng.latitude + ", " + endLatLng.longitude);
            showEndMarker(latLong);
            endPick.setText(
                    String.format("Điểm xuống: %.5f, %.5f",
                            latLong.latitude, latLong.longitude)
            );
        } else {
            clearSelection();
        }
    }

    private void showBeginMarker(LatLong latLong) {
        if (beginMarker != null) {
            mapView.getLayerManager().getLayers().remove(beginMarker);
        }

        var bitmap = AndroidGraphicFactory.convertToBitmap(
                getResources().getDrawable(R.drawable.location_on_2light, null)
        );

        beginMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight());
        mapView.getLayerManager().getLayers().add(beginMarker);
    }

    private void showEndMarker(LatLong latLong) {
        if (endMarker != null) {
            mapView.getLayerManager().getLayers().remove(endMarker);
        }

        var bitmap = AndroidGraphicFactory.convertToBitmap(
                getResources().getDrawable(R.drawable.location_on_2light, null)
        );

        endMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight());
        mapView.getLayerManager().getLayers().add(endMarker);
    }

    private void clearSelection() {
        if (beginMarker != null)
            mapView.getLayerManager().getLayers().remove(beginMarker);

        if (endMarker != null)
            mapView.getLayerManager().getLayers().remove(endMarker);

        beginMarker = null;
        endMarker = null;
        beginLatLng = null;
        endLatLng = null;

        beginPick.setText("Chọn điểm bắt đầu");
        endPick.setText("Chọn điểm kết thúc");
    }

}
