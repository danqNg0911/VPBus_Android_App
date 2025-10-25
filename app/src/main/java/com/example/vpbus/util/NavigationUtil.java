package com.example.vpbus.util;

import android.content.Context;
import android.content.Intent;

public class NavigationUtil {
    public static void goTo(Context context, Class<?> destination) {
        Intent intent = new Intent(context, destination);
        context.startActivity(intent);
    }
}
