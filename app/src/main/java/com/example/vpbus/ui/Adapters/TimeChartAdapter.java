package com.example.vpbus.ui.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeChartAdapter extends RecyclerView.Adapter<TimeChartAdapter.ViewHolder> {

    private Context context;
    private List<String> timeList;

    public TimeChartAdapter(Context context, List<String> timeList) {
        this.context = context;
        this.timeList = timeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_chart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("DEBUG_ADAPTER", "Đang vẽ item tại vị trí: " + position + " | Giờ: " + timeList.get(position));
        String timeStr = timeList.get(position);
        holder.tvDepartureTime.setText(timeStr);

        // Logic in đậm giờ sắp tới
        if (isNextTrip(timeStr, position)) {
            holder.tvDepartureTime.setTypeface(null, Typeface.BOLD);
            holder.tvDepartureTime.setTextColor(Color.BLACK);
            //holder.timelineIndicator.setBackgroundColor(Color.parseColor("#009688")); // Màu xanh nhấn
        } else {
            holder.tvDepartureTime.setTypeface(null, Typeface.NORMAL);
            holder.tvDepartureTime.setTextColor(Color.GRAY);
            //holder.timelineIndicator.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }
    }

    private boolean isNextTrip(String stopTime, int position) {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if (stopTime.compareTo(currentTime) >= 0) {
            if (position == 0) return true;
            return timeList.get(position - 1).compareTo(currentTime) < 0;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        int count = timeList != null ? timeList.size() : 0;
        // LOG 4: Kiểm tra RecyclerView có hỏi số lượng phần tử không
        Log.d("DEBUG_ADAPTER", "getItemCount gọi: " + count);
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepartureTime;
        //View timelineIndicator;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepartureTime = itemView.findViewById(R.id.tvDepartureTime);
            //timelineIndicator = itemView.findViewById(R.id.timelineIndicator);
        }
    }

    public void updateData(List<String> newTimes) {
        this.timeList.clear();
        this.timeList.addAll(newTimes);
        notifyDataSetChanged();
    }
}
