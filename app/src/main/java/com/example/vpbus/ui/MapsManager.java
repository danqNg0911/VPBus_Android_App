package com.example.vpbus.ui;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.example.vpbus.R;
import com.example.vpbus.model.BusShape;
import com.example.vpbus.model.BusStop;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsManager {
    private TileRendererLayer tileRendererLayer;
    private TileCache tileCache;
    private Polyline routePolyline;
    private List<Marker> stopMarkers = new ArrayList<>();

    public void initMap(Context context, MapView mapView, String mapFileName, String themeFileName) throws IOException {

        AndroidGraphicFactory.createInstance(context.getApplicationContext());
        mapView.setClickable(true);

        // Copy file .map
        File mapFile = copyMapFromAssets(context, mapFileName);
        MapDataStore mapDataStore = new MapFile(mapFile);

        // Cache
        tileCache = AndroidUtil.createTileCache(
                context,
                "mapcache",
                mapView.getModel().displayModel.getTileSize(),
                1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor()
        );

        // Renderer layer
        tileRendererLayer = new TileRendererLayer(
                tileCache,
                mapDataStore,
                mapView.getModel().mapViewPosition,
                AndroidGraphicFactory.INSTANCE
        );

        File themeFile = copyMapFromAssets(context, themeFileName);
        tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(themeFile));

        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        BoundingBox boundingBox = new BoundingBox(
                21.1382,   // minLatitude
                105.2203,  // minLongitude
                21.5317,   // maxLatitude
                105.6712   // maxLongitude
        );

        MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;

        mapViewPosition.setMapLimit(boundingBox);


    }

    public void setInitialPosition(MapView mapView, double lat, double lon, byte zoom) {
        mapView.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
        mapView.getModel().mapViewPosition.setZoomLevel(zoom);
    }

    public void onDestroy(MapView mapView) {
        if (tileRendererLayer != null) {
            tileRendererLayer.onDestroy();
        }
        if (tileCache != null) {
            tileCache.destroy();
        }
        if (mapView != null) {
            mapView.destroyAll();
        }
        AndroidGraphicFactory.clearResourceMemoryCache();
    }

    private File copyMapFromAssets(Context context, String assetName) throws IOException {
        File outFile = new File(context.getFilesDir(), assetName);
        if (!outFile.exists()) {
            try (InputStream in = context.getAssets().open(assetName);
                 OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
        return outFile;
    }

    public void drawRoutePolyline(
            MapView mapView,
            List<BusShape> busShape,
            int color,
            float width
    ) {

        // ===== 1. Nếu đã có tuyến cũ → xoá trước =====
        if (routePolyline != null) {
            mapView.getLayerManager().getLayers().remove(routePolyline);
            routePolyline = null;
        }

        // ===== 2. Tạo Paint định nghĩa style cho tuyến =====
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);                 // màu tuyến
        paint.setStrokeWidth(width);           // độ dày
        paint.setStyle(Style.STROKE);

        // ===== 3. Tạo polyline =====
        Polyline polyline = new Polyline(paint, AndroidGraphicFactory.INSTANCE);

        for (BusShape point : busShape) {

            // Lấy tọa độ từ entity BusStop
            double lat = point.getShape_pt_lat();
            double lon = point.getShape_pt_lon();

            // Tạo LatLong cho Mapsforge
            LatLong latlon = new LatLong(lat, lon);

            // Thêm vào polyline
            polyline.getLatLongs().add(latlon);
        }

        // ===== 5. Add polyline vào map =====
        mapView.getLayerManager().getLayers().add(polyline);

        // ===== 6. Lưu reference để quản lý sau này =====
        routePolyline = polyline;
    }

    public void clearRoutePolyline(MapView mapView) {
        if (routePolyline != null) {
            mapView.getLayerManager().getLayers().remove(routePolyline);
            routePolyline = null;
        }
    }

    public void drawBusStops(List<BusStop> stops, MapView mapView, Context context) {
        Bitmap stopBitmap = AndroidGraphicFactory.INSTANCE.convertToBitmap(
                ContextCompat.getDrawable(context, R.drawable.stop_point)
        );

        for (BusStop stop : stops) {
            LatLong latLong = new LatLong(
                    stop.getStop_lat(),
                    stop.getStop_lon()
            );

            Marker marker = new Marker(
                    latLong,
                    stopBitmap,
                    0,
                    -stopBitmap.getHeight() / 2
            );

            stopMarkers.add(marker);
            mapView.getLayerManager().getLayers().add(marker);
        }
    }

    public void clearBusStops(MapView mapView) {
        for (Marker marker : stopMarkers) {
            mapView.getLayerManager().getLayers().remove(marker);
        }
        stopMarkers.clear();
    }

    public void renderRoute(
            MapView mapView,
            List<BusShape> shapePoints,
            List<BusStop> stops,
            Context context
    ) {
        clearRoutePolyline(mapView);
        clearBusStops(mapView);

        drawRoutePolyline(mapView, shapePoints, Color.parseColor("#BB0000"), 8f);
        drawBusStops(stops, mapView, context);

        mapView.invalidate();
    }

}
