package com.example.vpbus.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.data.AppDatabase;
import com.example.vpbus.ui.Adapters.TimeChartAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RouteTimeChartFragment extends Fragment {
    private String routeShortName;
    private int directionId;
    private RecyclerView rvTimeChart;
    private TimeChartAdapter adapter;
    private List<String> displayTimes = new ArrayList<>();

    public static RouteTimeChartFragment newInstance(String shortName, int direction) {
        RouteTimeChartFragment fragment = new RouteTimeChartFragment();
        Bundle args = new Bundle();
        args.putString("short_name", shortName);
        args.putInt("direction", direction);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_chart, container, false);
        rvTimeChart = view.findViewById(R.id.rvTimeChart);
        rvTimeChart.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TimeChartAdapter(getContext(), displayTimes);
        rvTimeChart.setAdapter(adapter);

        if (getArguments() != null) {
            routeShortName = getArguments().getString("short_name");
            directionId = getArguments().getInt("direction", 0);
        }

        if (routeShortName != null) {
            loadData();
        }
        return view;
    }

    private void loadData() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                Log.d("DEBUG_TIME_CHART", "Đang truy vấn cho: " + routeShortName + " | Direction: " + directionId);
                // Lấy dữ liệu từ DAO
                List<String> times = db.busStopTimesDao().getDepartureTimes(routeShortName, directionId);


                List<String> tempTimes = new ArrayList<>();
                if (times != null) {
                    for (String t : times) {
                        // Chuẩn hóa "4:30:15" -> "04:30"
                        String raw = t.trim();
                        String normalized = raw.length() == 7 ? "0" + raw : raw;
                        if (normalized.length() >= 5) {
                            tempTimes.add(normalized.substring(0, 5));
                        }
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayTimes.clear();
                        displayTimes.addAll(tempTimes);
                        //Log.d("DEBUG_TIME_CHART", "Danh sách hiển thị (normalized): " + displayTimes.size() + " items");

                        if (adapter == null) {
                            adapter = new TimeChartAdapter(getContext(), displayTimes);
                            rvTimeChart.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        // Cuộn đến chuyến sắp tới
                        int nextIndex = findNextTripIndex(displayTimes);
                        if (nextIndex != -1) {
                            ((LinearLayoutManager)rvTimeChart.getLayoutManager())
                                    .scrollToPositionWithOffset(nextIndex, 0);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("TimeChart", "Error loading data: " + e.getMessage());
            }
        }).start();
    }

    private int findNextTripIndex(List<String> times) {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i).compareTo(currentTime) >= 0) return i;
        }
        return -1;
    }

    public void updateDirection(boolean newDirectionId) {
        int newDirect;
        if (newDirectionId) newDirect = 1;
        else newDirect = 0;
        this.directionId = newDirect;
        loadData(); // Hàm này sẽ gọi DAO với directionId mới và notifyAdapter
    }
}