package com.example.vpbus.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.data.FavoriteStopsRepository;
import com.example.vpbus.model.BusStop;

import java.util.Locale;

public class BusStopDetailActivity extends AppCompatActivity {
    public static final String EXTRA_STOP_ID = "extra_stop_id";
    public static final String EXTRA_DISTANCE_METERS = "extra_distance_meters";

    private FavoriteStopsRepository favoriteStopsRepository;
    private ImageButton buttonFavorite;
    private String stopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_detail);

        favoriteStopsRepository = new FavoriteStopsRepository(this);
        stopId = getIntent().getStringExtra(EXTRA_STOP_ID);
        double distanceMeters = getIntent().getDoubleExtra(EXTRA_DISTANCE_METERS, -1);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonFavorite = findViewById(R.id.buttonFavorite);
        TextView textStopName = findViewById(R.id.textStopName);
        TextView textStopId = findViewById(R.id.textStopId);
        TextView textStopLat = findViewById(R.id.textStopLat);
        TextView textStopLon = findViewById(R.id.textStopLon);
        TextView textStopDistance = findViewById(R.id.textStopDistance);

        buttonBack.setOnClickListener(v -> finish());

        if (stopId == null || stopId.isEmpty()) {
            Toast.makeText(this, "Kh\u00f4ng t\u00ecm th\u1ea5y m\u00e3 b\u1ebfn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BusStop stop = AppDatabase.getInstance(this).busStopsDao().getStopById(stopId);
        if (stop == null) {
            Toast.makeText(this, "Kh\u00f4ng t\u00ecm th\u1ea5y th\u00f4ng tin b\u1ebfn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textStopName.setText(stop.getStop_name());
        textStopId.setText("M\u00e3 b\u1ebfn: " + stop.getStop_id());
        textStopLat.setText(String.format(Locale.US, "Latitude: %.6f", stop.getStop_lat()));
        textStopLon.setText(String.format(Locale.US, "Longitude: %.6f", stop.getStop_lon()));
        if (distanceMeters >= 0) {
            textStopDistance.setVisibility(View.VISIBLE);
            textStopDistance.setText(String.format(Locale.US, "Kho\u1ea3ng c\u00e1ch: %.1f km", distanceMeters / 1000.0));
        }

        updateFavoriteIcon();
        buttonFavorite.setOnClickListener(v -> {
            boolean isFavorite = favoriteStopsRepository.toggleFavorite(stopId);
            updateFavoriteIcon();
            Toast.makeText(
                    this,
                    isFavorite ? "\u0110\u00e3 th\u00eam v\u00e0o y\u00eau th\u00edch" : "\u0110\u00e3 b\u1ecf y\u00eau th\u00edch",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void updateFavoriteIcon() {
        buttonFavorite.setImageResource(
                favoriteStopsRepository.isFavorite(stopId)
                        ? R.drawable.ic_favorite
                        : R.drawable.ic_favorite_border
        );
    }
}
