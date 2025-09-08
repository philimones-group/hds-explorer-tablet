package org.philimone.hds.explorer.main.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mapbox.common.MapboxOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.CameraOptions;

import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.R;

import java.util.ArrayList;

public class MapViewActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView txtPageTitle;
    private ArrayList<MapMarker> markersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_view);
        this.txtPageTitle = findViewById(R.id.txtPageTitle);
        this.mapView = findViewById(R.id.mapView);

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

        if (title != null) {
            this.txtPageTitle.setText(title);
        }
    }

    private void showMarkers() {

        Bitmap bitmapHouse = BitmapFactory.decodeResource(getResources(), R.mipmap.nui_household_marker);

        Log.d("bitmap", bitmapHouse+"");
        if (this.markersList != null) {

            // ✅ v11 way: get AnnotationPlugin from MapView (Java: AnnotationsKt.getAnnotations)
            AnnotationPlugin annotationPlugin = (AnnotationPlugin) mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (annotationPlugin == null) {
                Log.e("MapViewActivity", "AnnotationPlugin not available on MapView");
                return;
            }

            PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());


            //PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(mapView, new AnnotationConfig());

            for (MapMarker marker : this.markersList) {

                //Log.d("marker", ""+marker);

                PointAnnotationOptions options = new PointAnnotationOptions();
                options.withPoint(marker.getPoint());
                options.withIconImage(bitmapHouse);
                options.withTextField(marker.getTittle());
                options.withTextColor(getResources().getColor(R.color.black));
                options.withTextSize(12.0);
                options.withTextAnchor(TextAnchor.BOTTOM);
                options.withTextOffset(new ArrayList<Double>() {{ add(0.0); add(4.0); }});

                pointAnnotationManager.create(options);


            }

            if (this.markersList.size() > 0){
                MapMarker firstMarker = this.markersList.get(0);

                //scroll to this position
                scrollMapToMarker(firstMarker);
            }
        }
    }

    private void scrollMapToMarker(MapMarker marker) {
        if (mapView != null) {

            // Create a CameraPosition that focuses on the coordinates of the most recent marker.
            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(marker.getPoint())
                    .zoom(9.0) // You can adjust the zoom level as needed
                    .pitch(0.0) // Set the tilt angle if desired
                    .build();

            final CameraAnimationsPlugin camera = CameraAnimationsUtils.getCamera(mapView);
            camera.easeTo(cameraPosition, new MapAnimationOptions.Builder().duration(4000).build(), null);

        }
    }
}