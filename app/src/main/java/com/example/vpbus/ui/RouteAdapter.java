package com.example.vpbus.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vpbus.R;
import com.example.vpbus.model.BusRoute;
import com.example.vpbus.util.DrawUtil;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private Context context;
    private List<BusRoute> routeList;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteClick(BusRoute route);
    }

    public RouteAdapter(Context context, List<BusRoute> routeList, OnRouteClickListener listener) {
        this.context = context;
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRoute route = routeList.get(position);
        holder.tvRouteName.setText(route.getLong_name());

        Bitmap icon = DrawUtil.createBusIcon(context, route.getShort_name());
        holder.ivBusIcon.setImageBitmap(icon);

        holder.itemView.setOnClickListener(v -> listener.onRouteClick(route));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteName;
        ImageView ivBusIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.tvRouteName);
            ivBusIcon = itemView.findViewById(R.id.ivBusIcon);
        }
    }
}
