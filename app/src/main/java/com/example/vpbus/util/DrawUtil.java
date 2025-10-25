package com.example.vpbus.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.vpbus.R;

public class DrawUtil {
    public static Bitmap createBusIcon(Context context, String routeName) {
        int width = 80;
        int height = 40;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Drawable busDrawable = ContextCompat.getDrawable(context, R.drawable.ic_bus_icon);
        if (busDrawable != null) {
            int iconSize = 40;
            busDrawable.setBounds(0, 0, iconSize, iconSize + 6);
            busDrawable.draw(canvas);
        }

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(15);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText(routeName, 40, 28, textPaint); // Căn chỉnh chữ

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#BB0000"));       // Màu đường gạch
        linePaint.setStrokeWidth(2f);
        canvas.drawLine(5, 36,75, 36, linePaint);

        return bitmap;
    }

}
