package com.example.vpbus.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import com.example.vpbus.R;
import com.example.vpbus.util.NavigationUtil;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.io.IOException;

public class CheckSearch extends AppCompatActivity {
    private MapView mapView;
    private MapsManager mapsManager = new MapsManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.select_place);

        Button searchRoute = findViewById(R.id.searchRoute);
        Button back = findViewById(R.id.backButton);

        searchRoute.setOnClickListener(view ->
                NavigationUtil.goTo(this, SuggestRoute.class)
        );

        back.setOnClickListener(view ->
                NavigationUtil.goTo(this, Main.class)
        );

        AndroidGraphicFactory.createInstance(getApplication());
        mapView = findViewById(R.id.mapView);

        try {
            mapsManager.initMap(this, mapView, "vinhphuc_v5.map", "Elevate2.xml");
            mapsManager.setInitialPosition(mapView, 21.0278, 105.8342, (byte)12);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsManager.onDestroy(mapView);
    }
}
