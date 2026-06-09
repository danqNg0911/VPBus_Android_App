package com.example.vpbus.service;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.widget.Toast;

import com.example.vpbus.R;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

public class MapService {
    private Location lastKnownLocation;
    private Marker locationMarker;
    private boolean mapCenteredOnce = false;

    private final BoundingBox provinceBounds = new BoundingBox(
            21.1382,   // minLatitude
            105.2203,  // minLongitude
            21.5317,   // maxLatitude
            105.6712   // maxLongitude
    );

    private final LatLong mapCenter = new LatLong(
            (provinceBounds.minLatitude + provinceBounds.maxLatitude) / 2,
            (provinceBounds.minLongitude + provinceBounds.maxLongitude) / 2
    );

    public void showCurrentLocation(Location location, MapView mapView, Context context) {
        if (location == null) return;

        LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());

        // nếu ngoài tỉnh thì ko vẽ marker 
        if (!provinceBounds.contains(latLong)) {
            if (locationMarker != null) {
                mapView.getLayerManager().getLayers().remove(locationMarker);
                locationMarker = null;
            }
            return;
        }

        //marker xanh vị trí user nếu trong tỉnh
        if (locationMarker == null) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_blue_dot);
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            locationMarker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);

            mapView.getLayerManager().getLayers().add(locationMarker);
        } else {
            locationMarker.setLatLong(latLong);
        }
    }


    public void recenterToCurrentLocation(Location location, MapView mapView, Context context) {
        if (location != null) {
            LatLong userLatLong = new LatLong(location.getLatitude(), location.getLongitude());
            if (provinceBounds.contains(userLatLong)) {
                mapView.getModel().mapViewPosition.setCenter(userLatLong);
            } else {
                mapView.getModel().mapViewPosition.setCenter(mapCenter);
                if (context != null) {
                    Toast.makeText(context, "Vị trí của bạn nằm ngoài khu vực hỗ trợ của xe bus.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void centerMapOnce(Location location, MapView mapView, byte zoomLevel) {
        if (!mapCenteredOnce && location != null) {
            LatLong userLatLong = new LatLong(location.getLatitude(), location.getLongitude());

            if (provinceBounds.contains(userLatLong)) {
                mapView.getModel().mapViewPosition.setCenter(userLatLong);
            } else {
                mapView.getModel().mapViewPosition.setCenter(mapCenter);
            }
            mapView.getModel().mapViewPosition.setZoomLevel(zoomLevel);
            mapCenteredOnce = true;
        }
    }


}