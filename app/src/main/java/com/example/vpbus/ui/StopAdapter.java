package com.example.vpbus.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.model.BusStop;
import java.util.List;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder> {

    private Context context;
    private List<BusStop> stopList;

    public StopAdapter(Context context, List<BusStop> stopList) {
        this.context = context;
        this.stopList = stopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bus_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusStop stop = stopList.get(position);
        holder.tvStopName.setText(stop.getStop_name());
        double lat = (stop.getStop_lat());
        double lon = (stop.getStop_lon());
        holder.tvLat.setText("Lat: " + lat);
        holder.tvLon.setText("Lon: " + lon);
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStopName, tvLat, tvLon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvLat = itemView.findViewById(R.id.tvLat);
            tvLon = itemView.findViewById(R.id.tvLon);
        }
    }
}
