package com.example.vpbus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.Journey;
import com.example.vpbus.service.Router.TransitPlanner;
import com.example.vpbus.ui.Adapters.JourneyAdapter;
import com.example.vpbus.util.NavigationUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuggestRoute extends AppCompatActivity {

    private static final String TAG = "SuggestRoute";
    private JourneyAdapter adapter;
    private final List<Journey> journeyList = new ArrayList<>();
    private double beginLat;
    private double beginLng;
    private double endLat;
    private double endLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_trip);

        Button back = findViewById(R.id.backButton);
        back.setOnClickListener(view -> NavigationUtil.goTo(this, CheckSearch.class));

        beginLat = getIntent().getDoubleExtra("begin_lat", 0);
        beginLng = getIntent().getDoubleExtra("begin_lng", 0);
        endLat = getIntent().getDoubleExtra("end_lat", 0);
        endLng = getIntent().getDoubleExtra("end_lng", 0);

        Button text1 = findViewById(R.id.editTextText);
        text1.setText(String.format(Locale.US, "\u0110i\u1ec3m \u0111\u00f3n: %.5f, %.5f", beginLat, beginLng));

        Button text2 = findViewById(R.id.editTextText2);
        text2.setText(String.format(Locale.US, "\u0110i\u1ec3m xu\u1ed1ng: %.5f, %.5f", endLat, endLng));

        RecyclerView recyclerView = findViewById(R.id.rvSuggestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JourneyAdapter(this, journeyList);
        adapter.setOnJourneyClickListener((journey, position) -> {
            JourneySelectionStore.setSelectedJourney(journey, beginLat, beginLng, endLat, endLng);
            startActivity(new Intent(this, JourneyMapActivity.class));
        });
        recyclerView.setAdapter(adapter);

        findRoutes(beginLat, beginLng, endLat, endLng);
    }

    private void findRoutes(double latFrom, double lngFrom, double latTo, double lngTo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "Start routing");
                AppDatabase db = AppDatabase.getInstance(this);
                TransitPlanner planner = new TransitPlanner(db);
                Calendar startTime = Calendar.getInstance();

                List<Journey> journeyOptions = planner.planRoute(latFrom, lngFrom, latTo, lngTo, startTime);

                runOnUiThread(() -> {
                    journeyList.clear();
                    if (journeyOptions != null) {
                        journeyList.addAll(journeyOptions);
                    }
                    adapter.notifyDataSetChanged();

                    if (journeyList.isEmpty()) {
                        Toast.makeText(this, "Kh\u00f4ng t\u00ecm th\u1ea5y l\u1ed9 tr\u00ecnh ph\u00f9 h\u1ee3p", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Routing failed: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "L\u1ed7i khi t\u00ecm \u0111\u01b0\u1eddng", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
