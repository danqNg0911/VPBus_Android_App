package com.example.vpbus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.model.BusRoute;
import com.example.vpbus.util.NavigationUtil;

import java.util.List;

public class RouteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.route);

        Button suggest = findViewById(R.id.suggestButton);
        suggest.setOnClickListener(view -> {
            NavigationUtil.goTo(this, Main.class);
        });

        Button stops = findViewById(R.id.stopsButton);
        stops.setOnClickListener(view ->{
            NavigationUtil.goTo(this, StopsActivity.class);
        });

        //Button routeDetails = findViewById(R.id.route_details);
        // routeDetails.setOnClickListener(view ->{
//            NavigationUtil.goTo(this, RouteDetails.class);
//        });

        recyclerView = findViewById(R.id.recyclerViewRoutes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // Lấy dữ liệu từ Room (tránh chạy trên main thread nếu có nhiều)
        new Thread(() -> {
            List<BusRoute> routes = db.busRouteDao().getAllRoute();
            runOnUiThread(() -> {
                adapter = new RouteAdapter(this, routes, route ->{
                    Intent intent = new Intent(this, RouteDetails.class);
                    intent.putExtra("route_short_name", route.getShort_name());
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();

    }
}
