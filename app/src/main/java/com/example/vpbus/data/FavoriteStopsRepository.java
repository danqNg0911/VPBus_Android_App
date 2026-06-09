package com.example.vpbus.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoriteStopsRepository {
    private static final String PREFS_NAME = "favorite_stops";
    private static final String KEY_FAVORITE_STOP_IDS = "favorite_stop_ids";

    private final SharedPreferences preferences;

    public FavoriteStopsRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(String stopId) {
        return getFavoriteIds().contains(stopId);
    }

    public boolean toggleFavorite(String stopId) {
        Set<String> ids = getFavoriteIds();
        boolean isFavorite;
        if (ids.contains(stopId)) {
            ids.remove(stopId);
            isFavorite = false;
        } else {
            ids.add(stopId);
            isFavorite = true;
        }
        preferences.edit().putStringSet(KEY_FAVORITE_STOP_IDS, ids).apply();
        return isFavorite;
    }

    public Set<String> getFavoriteIds() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITE_STOP_IDS, new HashSet<>()));
    }
}
