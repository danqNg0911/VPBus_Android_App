package com.example.vpbus.service;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.function.Consumer;

public class LocationService {
    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    private final Activity activity;
    private final LocationManager locationManager;
    private LocationListener locationListener;

    public LocationService(Activity activity) {
        this.activity = activity;
        this.locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
    }

    public void requestLocationUpdates(@NonNull LocationListener listener) {
        this.locationListener = listener;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                5,
                locationListener
        );
    }

    public void stopLocationUpdates() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public void handlePermissionResult(int requestCode, @NonNull int[] grantResults, Runnable onGranted, Runnable onDenied) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onGranted.run();
            } else {
                onDenied.run();
            }
        }
    }

    public void getLastKnownLocation(@NonNull Consumer<Location> callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location cached = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (cached != null) {
            callback.accept(cached);
        }
    }

}
