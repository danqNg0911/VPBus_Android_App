package com.example.vpbus.ui.fragments;

import android.content.Context;
import android.os.Bundle;
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
import com.example.vpbus.model.BusStop;
import com.example.vpbus.ui.Adapters.StopAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteListStopsFragment extends Fragment {
    private static final String ARG_ROUTE_ID = "route_short_name";
    private String routeShortName;
    private RecyclerView recyclerView;
    private StopAdapter stopAdapter;
    private AppDatabase db;
    private final List<BusStop> currentStops = new ArrayList<>();
    private StopSelectionCallback callback;

    public interface StopSelectionCallback {
        void onStopSelected(BusStop stop);
    }

    public static RouteListStopsFragment newInstance(String routeShortName) {
        RouteListStopsFragment fragment = new RouteListStopsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeShortName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeShortName = getArguments().getString(ARG_ROUTE_ID);
        }
        db = AppDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_stopslist, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewStops);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        stopAdapter = new StopAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(stopAdapter);

        loadStops();
        stopAdapter.setOnStopClickListener((stop, position) -> {
            if (callback != null) callback.onStopSelected(stop);
        });
        return view;
    }

    private void loadStops() {
        new Thread(() -> {
            List<String> tripIds = db.tripDao().getTripIdsByShortName(routeShortName);
            if (tripIds == null || tripIds.isEmpty()) return;
            String tripId = tripIds.get(0);

            List<BusStop> stops = db.busStopsDao().getStopsByTrip(tripId);
            if (stops == null || stops.isEmpty()) return;

            requireActivity().runOnUiThread(() -> {
                currentStops.clear();
                currentStops.addAll(stops);

                stopAdapter.submitList(currentStops);
                recyclerView.setAdapter(stopAdapter);
            });
        }).start();
    }

    public void reverseStops() {
        if (!currentStops.isEmpty()) {
            Collections.reverse(currentStops);
            stopAdapter.submitList(currentStops);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof StopSelectionCallback) {
            callback = (StopSelectionCallback) context;
        }
    }


}

