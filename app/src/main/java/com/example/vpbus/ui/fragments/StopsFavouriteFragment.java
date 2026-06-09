package com.example.vpbus.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.ui.Adapters.StopAdapter;
import com.example.vpbus.ui.BusStopDetailActivity;
import com.example.vpbus.ui.StopsDataProvider;

import java.util.ArrayList;
import java.util.List;

public class StopsFavouriteFragment extends Fragment {
    private StopsDataProvider stopsDataProvider;
    private StopAdapter stopAdapter;
    private TextView emptyText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof StopsDataProvider) {
            stopsDataProvider = (StopsDataProvider) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops_fav, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFavoriteStops);
        emptyText = view.findViewById(R.id.textEmptyFavorites);

        stopAdapter = new StopAdapter(requireContext(), new ArrayList<>());
        stopAdapter.setOnStopClickListener((stop, position) -> openStopDetail(stop));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(stopAdapter);

        refresh();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (stopAdapter == null || stopsDataProvider == null) return;
        List<BusStop> stops = stopsDataProvider.getFavoriteStops();
        stopAdapter.submitList(stops);
        emptyText.setVisibility(stops.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openStopDetail(BusStop stop) {
        Intent intent = new Intent(requireContext(), BusStopDetailActivity.class);
        intent.putExtra(BusStopDetailActivity.EXTRA_STOP_ID, stop.getStop_id());
        if (stop.distance > 0) {
            intent.putExtra(BusStopDetailActivity.EXTRA_DISTANCE_METERS, stop.distance);
        }
        startActivity(intent);
    }
}
