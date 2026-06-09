package com.example.vpbus.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.model.JourneyLeg;

import java.util.List;
import java.util.Locale;

public class JourneyLegDetailAdapter extends RecyclerView.Adapter<JourneyLegDetailAdapter.ViewHolder> {
    private final Context context;
    private final List<JourneyLeg> legs;

    public JourneyLegDetailAdapter(Context context, List<JourneyLeg> legs) {
        this.context = context;
        this.legs = legs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_journey_leg_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JourneyLeg leg = legs.get(position);
        if ("walk".equals(leg.getType())) {
            holder.icon.setImageResource(R.drawable.ic_walk);
            holder.text.setText(String.format(Locale.US, "Di bo %.0f m", leg.getDistance()));
            return;
        }

        holder.icon.setImageResource(R.drawable.directions_bus);
        String routeId = String.valueOf(leg.getBusInfo().get("route_id"));
        String from = String.valueOf(leg.getBusInfo().get("from_stop"));
        String to = String.valueOf(leg.getBusInfo().get("to_stop"));
        String depart = String.valueOf(leg.getBusInfo().get("depart_time"));
        String arrive = String.valueOf(leg.getBusInfo().get("arrive_time"));
        holder.text.setText("Tuyen " + routeId + ": " + from + " (" + depart + ") -> " + to + " (" + arrive + ")");
    }

    @Override
    public int getItemCount() {
        return legs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.legIcon);
            text = itemView.findViewById(R.id.legText);
        }
    }
}
