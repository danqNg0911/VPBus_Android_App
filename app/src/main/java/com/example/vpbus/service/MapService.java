package com.example.vpbus.service;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.example.vpbus.R;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

public class MapService {
    private Location lastKnownLocation;
    private Marker locationMarker;
    private boolean mapCenteredOnce = false;

    public void showCurrentLocation(Location location, MapView mapView, Context context) {
        if (location == null) return;

        LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());

        // Nếu marker chưa tạo thì tạo mới
        if (locationMarker == null) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_blue_dot);
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            locationMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);

            mapView.getLayerManager().getLayers().add(locationMarker);
        } else {
            // Nếu đã có thì chỉ cần cập nhật vị trí
            locationMarker.setLatLong(latLong);
        }
    }


    public void recenterToCurrentLocation(Location location, MapView mapView) {
        if (location != null) {
            LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
            mapView.getModel().mapViewPosition.setCenter(latLong);
        }
    }

    public void centerMapOnce(Location location, MapView mapView, byte zoomLevel) {
        if (!mapCenteredOnce && location != null) {
            mapView.getModel().mapViewPosition.setCenter(
                    new LatLong(location.getLatitude(), location.getLongitude())
            );
            mapView.getModel().mapViewPosition.setZoomLevel(zoomLevel);
            mapCenteredOnce = true;
        }
    }


}