package com.example.vpbus.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.model.Journey;
import com.example.vpbus.model.JourneyLeg;
import com.example.vpbus.model.JourneySummary;

import java.util.List;
import java.util.Locale;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.ViewHolder> {
    private final Context context;
    private final List<Journey> journeys;
    private OnJourneyClickListener listener;

    public JourneyAdapter(Context context, List<Journey> journeys) {
        this.context = context;
        this.journeys = journeys;
    }

    public void setOnJourneyClickListener(OnJourneyClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggest_leg, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journey journey = journeys.get(position);
        List<JourneyLeg> legs = journey.legs;
        JourneySummary summary = journey.summary;

        setupLegsIcons(holder.legsIconContainer, legs);

        String depart = summary != null ? formatTime(summary.departSec) : firstBusTime(legs, "depart_time");
        String arrive = summary != null ? formatTime(summary.arriveSec) : firstBusTime(legs, "arrive_time");
        holder.tvTimeRange.setText(depart + " - " + arrive);

        String firstStop = firstBusStop(legs);
        holder.tvFirstInfo.setText("Khoi hanh luc " + depart + " tai ben " + firstStop);

        int durationMin = summary != null ? Math.max(1, summary.durationSec / 60) : 0;
        int price = summary != null ? summary.price : calculateBusLegCount(legs) * 10000;
        holder.tvTotalDuration.setText(durationMin + " phut");
        holder.tvTotalPrice.setText(String.format(Locale.US, "%,d d", price));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onJourneyClick(journeys.get(pos), pos);
            }
        });
    }

    private void setupLegsIcons(LinearLayout container, List<JourneyLeg> legs) {
        container.removeAllViews();
        for (int i = 0; i < legs.size(); i++) {
            JourneyLeg leg = legs.get(i);
            if ("walk".equals(leg.getType())) {
                addIconView(container, R.drawable.ic_walk);
            } else if ("bus".equals(leg.getType())) {
                String routeId = (String) leg.getBusInfo().get("route_id");
                addBusBadge(container, routeId);
            }

            if (i < legs.size() - 1) {
                addIconView(container, R.drawable.ic_chevron_right);
            }
        }
    }

    private void addIconView(LinearLayout container, int resId) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(resId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
        params.setMargins(4, 0, 4, 0);
        imageView.setLayoutParams(params);
        container.addView(imageView);
    }

    private void addBusBadge(LinearLayout container, String routeId) {
        View busBadge = LayoutInflater.from(context).inflate(R.layout.layout_bus_badge, container, false);
        TextView tvRoute = busBadge.findViewById(R.id.tvRouteId);
        tvRoute.setText(routeId);
        container.addView(busBadge);
    }

    private String firstBusTime(List<JourneyLeg> legs, String key) {
        for (JourneyLeg leg : legs) {
            if ("bus".equals(leg.getType())) {
                return String.valueOf(leg.getBusInfo().get(key));
            }
        }
        return "00:00";
    }

    private String firstBusStop(List<JourneyLeg> legs) {
        for (JourneyLeg leg : legs) {
            if ("bus".equals(leg.getType())) {
                return String.valueOf(leg.getBusInfo().get("from_stop"));
            }
        }
        return "";
    }

    private int calculateBusLegCount(List<JourneyLeg> legs) {
        int count = 0;
        for (JourneyLeg leg : legs) {
            if ("bus".equals(leg.getType())) count++;
        }
        return count;
    }

    private String formatTime(int seconds) {
        int normalized = ((seconds % 86400) + 86400) % 86400;
        int h = normalized / 3600;
        int m = (normalized % 3600) / 60;
        return String.format(Locale.US, "%02d:%02d", h, m);
    }

    @Override
    public int getItemCount() {
        return journeys.size();
    }

    public interface OnJourneyClickListener {
        void onJourneyClick(Journey journey, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout legsIconContainer;
        TextView tvTimeRange, tvFirstInfo, tvTotalDuration, tvTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            legsIconContainer = itemView.findViewById(R.id.legIconContainer);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvFirstInfo = itemView.findViewById(R.id.tvFirstInfo);
            tvTotalDuration = itemView.findViewById(R.id.tvTotalDuration);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrize);
        }
    }
}
