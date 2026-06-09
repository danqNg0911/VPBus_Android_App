package com.example.vpbus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.example.vpbus.R;
import com.example.vpbus.model.BusShape;
import com.example.vpbus.model.BusStop;
import com.example.vpbus.model.Journey;
import com.example.vpbus.model.JourneyLeg;
import com.example.vpbus.model.Trip;
import com.example.vpbus.service.Router.TransitData;

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
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsManager {
    private TileRendererLayer tileRendererLayer;
    private TileCache tileCache;
    private Polyline routePolyline;
    private final List<Polyline> journeyPolylines = new ArrayList<>();
    private final List<Marker> stopMarkers = new ArrayList<>();
    private final List<Marker> routeDirectionMarkers = new ArrayList<>();
    private final List<Marker> journeyMarkers = new ArrayList<>();
    private Marker highlightedMarker;
    private final Map<String, Marker> stopMarkerMap = new HashMap<>();
    private Bitmap normalStopBitmap;
    private Bitmap selectedStopBitmap;
    private final Handler markerHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingMarkerBatch;

    private final BoundingBox provinceBounds = new BoundingBox(
            21.1382,
            105.2203,
            21.5317,
            105.6712
    );

    public void initMap(Context context, MapView mapView, String mapFileName, String themeFileName) throws IOException {
        AndroidGraphicFactory.createInstance(context.getApplicationContext());
        mapView.setClickable(true);

        File mapFile = copyMapFromAssets(context, mapFileName);
        MapDataStore mapDataStore = new MapFile(mapFile);

        tileCache = AndroidUtil.createTileCache(
                context,
                "mapcache",
                mapView.getModel().displayModel.getTileSize(),
                1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor()
        );

        tileRendererLayer = new TileRendererLayer(
                tileCache,
                mapDataStore,
                mapView.getModel().mapViewPosition,
                AndroidGraphicFactory.INSTANCE
        );

        File themeFile = copyMapFromAssets(context, themeFileName);
        tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(themeFile));

        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        double centerLat = (provinceBounds.minLatitude + provinceBounds.maxLatitude) / 2;
        double centerLon = (provinceBounds.minLongitude + provinceBounds.maxLongitude) / 2;
        mapView.getModel().mapViewPosition.setCenter(new LatLong(centerLat, centerLon));
        mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);
    }

    public void setInitialPosition(MapView mapView, double lat, double lon, byte zoom) {
        mapView.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
        mapView.getModel().mapViewPosition.setZoomLevel(zoom);
    }

    public void setSafeUserLocation(MapView mapView, double userLat, double userLon, byte zoom) {
        LatLong userLocation = new LatLong(userLat, userLon);
        if (provinceBounds.contains(userLocation)) {
            setInitialPosition(mapView, userLat, userLon, zoom);
            return;
        }

        double centerLat = (provinceBounds.minLatitude + provinceBounds.maxLatitude) / 2;
        double centerLon = (provinceBounds.minLongitude + provinceBounds.maxLongitude) / 2;
        setInitialPosition(mapView, centerLat, centerLon, zoom);
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

    public void drawRoutePolyline(MapView mapView, List<BusShape> busShape, int color, float width) {
        if (routePolyline != null) {
            mapView.getLayerManager().getLayers().remove(routePolyline);
            routePolyline = null;
        }

        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Style.STROKE);

        Polyline polyline = new Polyline(paint, AndroidGraphicFactory.INSTANCE);
        for (BusShape point : busShape) {
            polyline.getLatLongs().add(new LatLong(point.getShape_pt_lat(), point.getShape_pt_lon()));
        }

        mapView.getLayerManager().getLayers().add(polyline);
        routePolyline = polyline;
    }

    public void clearRoutePolyline(MapView mapView) {
        if (routePolyline != null) {
            mapView.getLayerManager().getLayers().remove(routePolyline);
            routePolyline = null;
        }
        clearRouteDirectionMarkers(mapView);
    }

    public void drawBusStops(List<BusStop> stops, MapView mapView, Context context) {
        ensureStopBitmaps(context);
        stopMarkers.clear();
        stopMarkerMap.clear();

        for (BusStop stop : stops) {
            Marker marker = newStopMarker(stop, normalStopBitmap);
            stopMarkers.add(marker);
            stopMarkerMap.put(stop.getStop_id(), marker);
            mapView.getLayerManager().getLayers().add(marker);
        }
    }

    public void drawBusStopsLazy(List<BusStop> stops, MapView mapView, Context context) {
        ensureStopBitmaps(context);
        clearBusStops(mapView);

        final int batchSize = 25;
        pendingMarkerBatch = new Runnable() {
            private int index = 0;

            @Override
            public void run() {
                int end = Math.min(index + batchSize, stops.size());
                for (; index < end; index++) {
                    BusStop stop = stops.get(index);
                    Marker marker = newStopMarker(stop, normalStopBitmap);
                    stopMarkers.add(marker);
                    stopMarkerMap.put(stop.getStop_id(), marker);
                    mapView.getLayerManager().getLayers().add(marker);
                }
                mapView.invalidate();
                if (index < stops.size()) {
                    markerHandler.postDelayed(this, 16);
                }
            }
        };
        markerHandler.post(pendingMarkerBatch);
    }

    public void clearBusStops(MapView mapView) {
        if (pendingMarkerBatch != null) {
            markerHandler.removeCallbacks(pendingMarkerBatch);
            pendingMarkerBatch = null;
        }
        for (Marker marker : stopMarkers) {
            mapView.getLayerManager().getLayers().remove(marker);
        }
        stopMarkers.clear();
        stopMarkerMap.clear();
        clearHighlightedMarker(mapView);
    }

    public void renderRoute(MapView mapView, List<BusShape> shapePoints, List<BusStop> stops, Context context) {
        clearRoutePolyline(mapView);
        clearBusStops(mapView);

        drawRoutePolyline(mapView, shapePoints, Color.parseColor("#BB0000"), 8f);
        drawBusStops(stops, mapView, context);
        drawRouteDirectionArrows(mapView, shapePoints, context);

        mapView.invalidate();
    }

    public void renderJourney(
            MapView mapView,
            Journey journey,
            TransitData data,
            double startLat,
            double startLon,
            double endLat,
            double endLon,
            Context context
    ) {
        clearJourney(mapView);

        int busLegIndex = 0;
        for (JourneyLeg leg : journey.legs) {
            if ("walk".equals(leg.getType())) {
                drawWalkLeg(mapView, leg, data);
            } else if ("bus".equals(leg.getType())) {
                int color = getJourneyBusColor(busLegIndex);
                List<LatLong> points = buildBusLegPoints(leg, data);
                drawLatLongPolyline(mapView, points, color, 8f, false);
                drawDirectionArrows(mapView, points, context, color);
                addBoardAlightMarkers(mapView, leg, data, context);
                busLegIndex++;
            }
        }

        addJourneyMarker(mapView, new LatLong(startLat, startLon), R.drawable.ic_blue_dot, context, true);
        addJourneyMarker(mapView, new LatLong(endLat, endLon), R.drawable.location_on_2light, context, false);
        mapView.getModel().mapViewPosition.setCenter(new LatLong(startLat, startLon));
        mapView.getModel().mapViewPosition.setZoomLevel((byte) 14);
        mapView.invalidate();
    }

    public void clearJourney(MapView mapView) {
        for (Polyline polyline : journeyPolylines) {
            mapView.getLayerManager().getLayers().remove(polyline);
        }
        journeyPolylines.clear();

        for (Marker marker : journeyMarkers) {
            mapView.getLayerManager().getLayers().remove(marker);
        }
        journeyMarkers.clear();
    }

    public void focusOnStop(MapView mapView, BusStop stop, Context context) {
        focusOnStop(mapView, stop, context, 0);
    }

    public void focusOnStop(MapView mapView, BusStop stop, Context context, int verticalOffsetPixels) {
        ensureStopBitmaps(context);

        mapView.getModel().mapViewPosition.setCenter(
                getOffsetCenter(mapView, stop, verticalOffsetPixels)
        );

        clearHighlightedMarker(mapView);

        Marker highlightMarker = newStopMarker(stop, selectedStopBitmap);
        mapView.getLayerManager().getLayers().add(highlightMarker);
        highlightedMarker = highlightMarker;

        mapView.invalidate();
    }

    private Marker newStopMarker(BusStop stop, Bitmap bitmap) {
        return new Marker(
                new LatLong(stop.getStop_lat(), stop.getStop_lon()),
                bitmap,
                0,
                -bitmap.getHeight() / 2
        );
    }

    private void ensureStopBitmaps(Context context) {
        if (normalStopBitmap == null) {
            normalStopBitmap = createScaledBitmap(context, R.drawable.stop_point, 0.5f);
        }

        if (selectedStopBitmap == null) {
            selectedStopBitmap = createScaledBitmap(context, R.drawable.stop_point_2light, 0.5f);
        }
    }

    private Bitmap createScaledBitmap(Context context, int drawableResId, float scale) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable == null) return null;

        int width = Math.max(1, Math.round(drawable.getIntrinsicWidth() * scale));
        int height = Math.max(1, Math.round(drawable.getIntrinsicHeight() * scale));
        android.graphics.Bitmap androidBitmap = android.graphics.Bitmap.createBitmap(
                width,
                height,
                android.graphics.Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(androidBitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return AndroidGraphicFactory.convertToBitmap(
                new BitmapDrawable(context.getResources(), androidBitmap)
        );
    }

    private LatLong getOffsetCenter(MapView mapView, BusStop stop, int verticalOffsetPixels) {
        if (verticalOffsetPixels <= 0) {
            return new LatLong(stop.getStop_lat(), stop.getStop_lon());
        }

        byte zoom = mapView.getModel().mapViewPosition.getZoomLevel();
        double metersPerPixel = 156543.03392
                * Math.cos(Math.toRadians(stop.getStop_lat()))
                / Math.pow(2, zoom);
        double offsetMeters = verticalOffsetPixels * metersPerPixel;
        double offsetLatitude = offsetMeters / 111320.0;

        return new LatLong(stop.getStop_lat() - offsetLatitude, stop.getStop_lon());
    }

    private void clearHighlightedMarker(MapView mapView) {
        if (highlightedMarker != null) {
            mapView.getLayerManager().getLayers().remove(highlightedMarker);
            highlightedMarker = null;
        }
    }

    private void drawRouteDirectionArrows(MapView mapView, List<BusShape> shapePoints, Context context) {
        clearRouteDirectionMarkers(mapView);
        if (shapePoints == null || shapePoints.size() < 2) return;

        int arrowCount = Math.min(12, Math.max(3, shapePoints.size() / 45));
        int step = Math.max(1, shapePoints.size() / (arrowCount + 1));

        for (int index = step; index < shapePoints.size() - 1; index += step) {
            BusShape from = shapePoints.get(index - 1);
            BusShape to = shapePoints.get(index + 1);
            double bearing = bearing(
                    from.getShape_pt_lat(),
                    from.getShape_pt_lon(),
                    to.getShape_pt_lat(),
                    to.getShape_pt_lon()
            );
            Bitmap arrowBitmap = createArrowBitmap(context, bearing);
            Marker marker = new Marker(
                    new LatLong(to.getShape_pt_lat(), to.getShape_pt_lon()),
                    arrowBitmap,
                    0,
                    0
            );
            routeDirectionMarkers.add(marker);
            mapView.getLayerManager().getLayers().add(marker);
        }
    }

    private void drawWalkLeg(MapView mapView, JourneyLeg leg, TransitData data) {
        List<LatLong> points = new ArrayList<>();
        if (leg.getPath() == null) return;

        for (Integer nodeId : leg.getPath()) {
            double[] coord = data.nodes.get(nodeId);
            if (coord != null) {
                points.add(new LatLong(coord[0], coord[1]));
            }
        }
        drawLatLongPolyline(mapView, points, Color.parseColor("#888888"), 6f, true);
    }

    private List<LatLong> buildBusLegPoints(JourneyLeg leg, TransitData data) {
        List<LatLong> points = new ArrayList<>();
        String tripId = String.valueOf(leg.getBusInfo().get("trip_id"));
        String fromStopId = String.valueOf(leg.getBusInfo().get("from_stop"));
        String toStopId = String.valueOf(leg.getBusInfo().get("to_stop"));

        Trip trip = data.tripsById.get(tripId);
        if (trip != null) {
            List<BusShape> shapePoints = data.shapesById.get(trip.getShape_id());
            BusStop fromStop = data.stopsById.get(fromStopId);
            BusStop toStop = data.stopsById.get(toStopId);
            if (shapePoints != null && fromStop != null && toStop != null) {
                List<BusShape> segment = sliceShape(shapePoints, fromStop, toStop);
                for (BusShape shape : segment) {
                    points.add(new LatLong(shape.getShape_pt_lat(), shape.getShape_pt_lon()));
                }
            }
        }

        if (points.isEmpty()) {
            BusStop fromStop = data.stopsById.get(fromStopId);
            BusStop toStop = data.stopsById.get(toStopId);
            if (fromStop != null && toStop != null) {
                points.add(new LatLong(fromStop.getStop_lat(), fromStop.getStop_lon()));
                points.add(new LatLong(toStop.getStop_lat(), toStop.getStop_lon()));
            }
        }
        return points;
    }

    private List<BusShape> sliceShape(List<BusShape> shapePoints, BusStop fromStop, BusStop toStop) {
        int fromIndex = nearestShapeIndex(shapePoints, fromStop.getStop_lat(), fromStop.getStop_lon());
        int toIndex = nearestShapeIndex(shapePoints, toStop.getStop_lat(), toStop.getStop_lon());
        if (fromIndex < 0 || toIndex < 0) return new ArrayList<>();

        List<BusShape> segment = new ArrayList<>();
        if (fromIndex <= toIndex) {
            segment.addAll(shapePoints.subList(fromIndex, toIndex + 1));
        } else {
            for (int i = fromIndex; i >= toIndex; i--) {
                segment.add(shapePoints.get(i));
            }
        }
        return segment;
    }

    private int nearestShapeIndex(List<BusShape> shapePoints, double lat, double lon) {
        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < shapePoints.size(); i++) {
            BusShape shape = shapePoints.get(i);
            double dLat = shape.getShape_pt_lat() - lat;
            double dLon = shape.getShape_pt_lon() - lon;
            double distance = dLat * dLat + dLon * dLon;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private void drawLatLongPolyline(MapView mapView, List<LatLong> points, int color, float width, boolean dashed) {
        if (points == null || points.size() < 2) return;

        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Style.STROKE);
        if (dashed) {
            paint.setDashPathEffect(new float[]{14f, 10f});
        }

        Polyline polyline = new Polyline(paint, AndroidGraphicFactory.INSTANCE);
        polyline.getLatLongs().addAll(points);
        journeyPolylines.add(polyline);
        mapView.getLayerManager().getLayers().add(polyline);
    }

    private void drawDirectionArrows(MapView mapView, List<LatLong> points, Context context, int color) {
        if (points == null || points.size() < 2) return;
        int arrowCount = Math.min(8, Math.max(1, points.size() / 35));
        int step = Math.max(1, points.size() / (arrowCount + 1));

        for (int index = step; index < points.size() - 1; index += step) {
            LatLong from = points.get(index - 1);
            LatLong to = points.get(index + 1);
            Bitmap arrowBitmap = createArrowBitmap(context, bearing(from.latitude, from.longitude, to.latitude, to.longitude), color);
            Marker marker = new Marker(to, arrowBitmap, 0, 0);
            journeyMarkers.add(marker);
            mapView.getLayerManager().getLayers().add(marker);
        }
    }

    private void addBoardAlightMarkers(MapView mapView, JourneyLeg leg, TransitData data, Context context) {
        BusStop fromStop = data.stopsById.get(String.valueOf(leg.getBusInfo().get("from_stop")));
        BusStop toStop = data.stopsById.get(String.valueOf(leg.getBusInfo().get("to_stop")));
        if (fromStop != null) {
            addJourneyMarker(mapView, new LatLong(fromStop.getStop_lat(), fromStop.getStop_lon()), R.drawable.stop_point_2light, context, false);
        }
        if (toStop != null) {
            addJourneyMarker(mapView, new LatLong(toStop.getStop_lat(), toStop.getStop_lon()), R.drawable.stop_point_2light, context, false);
        }
    }

    private void addJourneyMarker(MapView mapView, LatLong latLong, int drawableResId, Context context, boolean centerAnchor) {
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(context, drawableResId));
        Marker marker = new Marker(
                latLong,
                bitmap,
                0,
                centerAnchor ? -bitmap.getHeight() / 2 : -bitmap.getHeight()
        );
        journeyMarkers.add(marker);
        mapView.getLayerManager().getLayers().add(marker);
    }

    private int getJourneyBusColor(int busLegIndex) {
        switch (busLegIndex) {
            case 0:
                return Color.parseColor("#1565C0");
            case 1:
                return Color.parseColor("#FBC02D");
            case 2:
                return Color.parseColor("#2E7D32");
            default:
                return Color.parseColor("#1565C0");
        }
    }

    private void clearRouteDirectionMarkers(MapView mapView) {
        for (Marker marker : routeDirectionMarkers) {
            mapView.getLayerManager().getLayers().remove(marker);
        }
        routeDirectionMarkers.clear();
    }

    private Bitmap createArrowBitmap(Context context, double bearing) {
        return createArrowBitmap(context, bearing, Color.parseColor("#BB0000"));
    }

    private Bitmap createArrowBitmap(Context context, double bearing, int color) {
        int size = Math.round(24 * context.getResources().getDisplayMetrics().density);
        android.graphics.Bitmap androidBitmap = android.graphics.Bitmap.createBitmap(
                size,
                size,
                android.graphics.Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(androidBitmap);
        canvas.rotate((float) bearing - 90f, size / 2f, size / 2f);

        android.graphics.Paint fill = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        fill.setColor(color);
        fill.setStyle(android.graphics.Paint.Style.FILL);

        android.graphics.Paint stroke = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(android.graphics.Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f, size / 12f));

        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(size * 0.78f, size * 0.50f);
        path.lineTo(size * 0.32f, size * 0.24f);
        path.lineTo(size * 0.32f, size * 0.76f);
        path.close();

        canvas.drawPath(path, fill);
        canvas.drawPath(path, stroke);

        return AndroidGraphicFactory.convertToBitmap(
                new BitmapDrawable(context.getResources(), androidBitmap)
        );
    }

    private double bearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLon) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2)
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLon);

        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
    }
}
