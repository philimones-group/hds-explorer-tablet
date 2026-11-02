package org.philimone.hds.explorer.main.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.CameraOptions;

import org.philimone.hds.explorer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapViewActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView txtPageTitle;
    private LinearLayout panelMarkerInfo;
    private TextView txtMarkerTitle;
    private TextView txtMarkerDescription;
    private TextView txtMarkerCoords;
    private Button btnGetDirections;

    private ArrayList<MapMarker> markersList;
    private boolean isSingleMarker;

    private final Map<String, MapMarker> annotationMap = new HashMap<>();
    private MapMarker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_view);
        this.txtPageTitle = findViewById(R.id.txtPageTitle);
        this.mapView = findViewById(R.id.mapView);

        this.panelMarkerInfo = findViewById(R.id.panelMarkerInfo);
        this.txtMarkerTitle = findViewById(R.id.txtMarkerTitle);
        this.txtMarkerDescription = findViewById(R.id.txtMarkerDescription);
        this.txtMarkerCoords = findViewById(R.id.txtMarkerCoords);
        this.btnGetDirections = findViewById(R.id.btnGetDirections);

        if (this.btnGetDirections != null) {
            this.btnGetDirections.setOnClickListener(v -> {
                if (selectedMarker != null) {
                    openExternalDirections(selectedMarker);
                }
            });
        }

        initialize();

        this.mapView.getMapboxMap().loadStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                showMarkers();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.mapView != null) mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mapView != null) mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (this.mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mapView != null) mapView.onDestroy();
    }

    private void initialize(){
        // Retrieve the list of MarkerOptions from the Intent
        Intent intent = getIntent();

        String title = intent.getStringExtra("pageTitle");
        this.markersList = intent.getParcelableArrayListExtra("markersList");

        if (this.markersList != null) {
            this.isSingleMarker = markersList.size() == 1;
        }

        if (title != null) {
            this.txtPageTitle.setText(title);
        }
    }

    private void showMarkers() {

        Bitmap bitmapHouse = BitmapFactory.decodeResource(getResources(), R.mipmap.nui_household_marker);

        Log.d("bitmap", bitmapHouse+"");
        if (this.markersList != null) {

            // ✅ v11 way: get AnnotationPlugin from MapView
            AnnotationPlugin annotationPlugin = (AnnotationPlugin) mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (annotationPlugin == null) {
                Log.e("MapViewActivity", "AnnotationPlugin not available on MapView");
                return;
            }

            PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
            annotationMap.clear();

            for (MapMarker marker : this.markersList) {

                PointAnnotationOptions options = new PointAnnotationOptions();
                options.withPoint(marker.getPoint());
                options.withIconImage(bitmapHouse);
                options.withTextField(marker.getTittle());
                options.withTextColor(getResources().getColor(R.color.black));
                options.withTextSize(12.0);
                options.withTextAnchor(TextAnchor.BOTTOM);
                options.withTextOffset(new ArrayList<Double>() {{ add(0.0); add(4.0); }});

                PointAnnotation annotation = pointAnnotationManager.create(options);
                annotationMap.put(annotation.getId(), marker);
            }

            pointAnnotationManager.addClickListener(annotation -> {
                MapMarker marker = annotationMap.get(annotation.getId());
                if (marker != null) {
                    selectMarker(marker);
                }
                return true;
            });

            if (this.markersList.size() > 0){
                MapMarker firstMarker = this.markersList.get(0);

                // Auto-select and scroll to marker
                selectMarker(firstMarker);
            }
        }
    }

    private void selectMarker(MapMarker marker) {
        if (marker == null) return;
        this.selectedMarker = marker;

        if (this.txtMarkerTitle != null) {
            this.txtMarkerTitle.setText(marker.getTittle() != null ? marker.getTittle() : "");
        }

        if (this.txtMarkerDescription != null) {
            String desc = marker.getDescription();
            if (desc != null && !desc.trim().isEmpty()) {
                this.txtMarkerDescription.setText(desc);
                this.txtMarkerDescription.setVisibility(View.VISIBLE);
            } else {
                this.txtMarkerDescription.setVisibility(View.GONE);
            }
        }

        if (this.txtMarkerCoords != null) {
            String coordsStr = String.format(java.util.Locale.US, "GPS: %.6f, %.6f", marker.getGpsLatitude(), marker.getGpsLongitude());
            this.txtMarkerCoords.setText(coordsStr);
        }

        if (this.panelMarkerInfo != null) {
            this.panelMarkerInfo.setVisibility(View.VISIBLE);
        }

        scrollMapToMarker(marker);
    }

    private void openExternalDirections(MapMarker marker) {
        if (marker == null) return;

        double lat = marker.getGpsLatitude();
        double lng = marker.getGpsLongitude();
        String title = Uri.encode(marker.getTittle() != null ? marker.getTittle() : "Destination");

        // Use geo intent so user can select Google Maps, OsmAnd, Waze, etc.
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + title + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        try {
            Intent chooserIntent = Intent.createChooser(mapIntent, getString(R.string.map_navigate_with_lbl));
            startActivity(chooserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.map_no_navigation_app_found_lbl), Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollMapToMarker(MapMarker marker) {
        if (mapView != null && marker != null) {

            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(marker.getPoint())
                    .zoom(9.0)
                    .pitch(0.0)
                    .build();

            final CameraAnimationsPlugin camera = CameraAnimationsUtils.getCamera(mapView);
            camera.easeTo(cameraPosition, new MapAnimationOptions.Builder().duration(4000).build(), null);

        }
    }
}
