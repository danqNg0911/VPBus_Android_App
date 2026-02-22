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

import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.ViewHolder> {
    private Context context;
    private List<Journey> journeys;

    public JourneyAdapter(Context context, List<Journey> journeys) {
        this.context = context;
        this.journeys = journeys;
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

        // 1. Xử lý nạp các Icon Legs động (Line 1 bên trái)
        setupLegsIcons(holder.legsIconContainer, legs);

        // 2. Xử lý Thời gian (Line 2 bên trái)
        String startTime = "00:00";
        String endTime = "00:00";
        String firstStop = "";

        // Tìm chặng bus đầu và cuối để lấy thời gian
        for (JourneyLeg leg : legs) {
            if (leg.getType().equals("bus")) {
                if (firstStop.isEmpty()) {
                    startTime = (String) leg.getBusInfo().get("depart_time");
                    firstStop = (String) leg.getBusInfo().get("from_stop");
                }
                endTime = (String) leg.getBusInfo().get("arrive_time");
            }
        }
        holder.tvTimeRange.setText(startTime + " - " + endTime);

        // 3. Xử lý Thông tin bến (Line 3 bên trái)
        holder.tvFirstInfo.setText("Khởi hành lúc " + startTime + " tại bến " + firstStop);

        // 4. Xử lý Tổng thời gian & Giá tiền (Bên phải)
        holder.tvTotalDuration.setText(calculateDuration(startTime, endTime) + " phút");
        holder.tvTotalPrice.setText(calculatePrice(legs) + " đ");
    }

    private void setupLegsIcons(LinearLayout container, List<JourneyLeg> legs) {
        container.removeAllViews(); // Xóa view cũ khi recycle

        for (int i = 0; i < legs.size(); i++) {
            JourneyLeg leg = legs.get(i);

            if (leg.getType().equals("walk")) {
                addIconView(container, R.drawable.ic_walk); // Icon người đi bộ
            } else if (leg.getType().equals("bus")) {
                String routeId = (String) leg.getBusInfo().get("route_id");
                addBusBadge(container, routeId); // Icon bus + Số hiệu tuyến
            }

            // Thêm dấu mũi tên ">" giữa các chặng, trừ chặng cuối
            if (i < legs.size() - 1) {
                addIconView(container, R.drawable.ic_chevron_right);
            }
        }
    }

    private void addIconView(LinearLayout container, int resId) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(resId);
        // Thiết lập kích thước icon nhỏ gọn
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
        params.setMargins(4, 0, 4, 0);
        imageView.setLayoutParams(params);
        container.addView(imageView);
    }

    private void addBusBadge(LinearLayout container, String routeId) {
        // Inflate một layout nhỏ cho bus badge (gồm icon bus + text số hiệu)
        View busBadge = LayoutInflater.from(context).inflate(R.layout.layout_bus_badge, container, false);
        TextView tvRoute = busBadge.findViewById(R.id.tvRouteId);
        tvRoute.setText(routeId);
        container.addView(busBadge);
    }

    private int calculateDuration(String start, String end) {
        String[] s = start.split(":");
        int startSec = Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60 + Integer.parseInt(s[2]);;
        String[] e = end.split(":");
        int endSec = Integer.parseInt(e[0]) * 3600 + Integer.parseInt(e[1]) * 60 + Integer.parseInt(e[2]);;
        return (endSec - startSec) / 60;
    }

    private String calculatePrice(List<JourneyLeg> legs) {
        int count = 0;
        for (JourneyLeg l : legs) if (l.getType().equals("bus")) count++;
        return String.format("%,d", count * 10000);
    }

    @Override
    public int getItemCount() { return journeys.size(); }

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
