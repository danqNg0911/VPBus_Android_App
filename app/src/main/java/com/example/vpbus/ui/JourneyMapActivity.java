package com.example.vpbus.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.Journey;
import com.example.vpbus.service.Router.TransitData;
import com.example.vpbus.ui.Adapters.JourneyLegDetailAdapter;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.io.IOException;

public class JourneyMapActivity extends AppCompatActivity {
    private final MapsManager mapsManager = new MapsManager();
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.activity_journey_map);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        Journey journey = JourneySelectionStore.getSelectedJourney();
        if (journey == null) {
            Toast.makeText(this, "Kh\u00f4ng t\u00ecm th\u1ea5y h\u00e0nh tr\u00ecnh", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mapView = findViewById(R.id.mapView);
        try {
            mapsManager.initMap(this, mapView, "vinhphuc_v5.map", "Elevate2.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        RecyclerView legsList = findViewById(R.id.rvJourneyLegs);
        legsList.setLayoutManager(new LinearLayoutManager(this));
        legsList.setAdapter(new JourneyLegDetailAdapter(this, journey.legs));

        TransitData data = TransitData.loadAll(AppDatabase.getInstance(this));
        mapsManager.renderJourney(
                mapView,
                journey,
                data,
                JourneySelectionStore.getStartLat(),
                JourneySelectionStore.getStartLon(),
                JourneySelectionStore.getEndLat(),
                JourneySelectionStore.getEndLon(),
                this
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsManager.onDestroy(mapView);
    }
}
