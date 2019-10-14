package com.example.hmsdemo.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Parcel;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.libraries.places.api.Places;
import com.huawei.hms.libraries.places.api.model.LocationRestriction;
import com.huawei.hms.libraries.places.api.model.Place;
import com.huawei.hms.libraries.places.api.model.PlaceLikelihood;
import com.huawei.hms.libraries.places.api.model.RectangularBounds;
import com.huawei.hms.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.huawei.hms.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.huawei.hms.libraries.places.api.net.FindCurrentPlaceRequest;
import com.huawei.hms.libraries.places.api.net.FindCurrentPlaceResponse;
import com.huawei.hms.libraries.places.api.net.PlacesClient;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.CircleOptions;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.MapStyleOptions;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.VisibleRegion;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class MapDemo extends BaseActivity implements OnMapReadyCallback, HuaweiMap.OnMapClickListener{

    private static final String TAG = "MapViewDemoActivity";
    private HuaweiMap hmap;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static float DEFAULT_ZOOM = (float) 11.0;
    private LatLng currentLatLng;
    private boolean trafficEnabled;
    private Circle mCircle;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private EditText query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_demo);

        query = (EditText)findViewById(R.id.queryDetail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        trafficEnabled = false;
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_map_demo);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mMapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        placesClient = Places.createClient(this);

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(HuaweiMap map) {
        Log.d(TAG, "onMapReady: ");
        hmap = map;
        hmap.setMyLocationEnabled(true);
        hmap.getUiSettings().setZoomControlsEnabled(true);
        hmap.getUiSettings().setMyLocationButtonEnabled(true);
        hmap.getUiSettings().setCompassEnabled(true);
        hmap.getUiSettings().setAllGesturesEnabled(true);
        //hmap.setMapStyle(HuaweiMap.MAP_TYPE_NORMAL);

        //Set a tap event listener for a map.
        hmap.setOnMapClickListener(this);

        // get current locaiton
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        showLog("getting current location" );
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            } else if (location == null) {
                                return;
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Location", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e) {
            Log.e(TAG, "getLastLocation exception:" + e.getMessage());
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        showLog("tapped, point=" + latLng);
        Point point = hmap.getProjection().toScreenLocation(latLng);
        showLog("to point, point=" + point);
        LatLng newLatlng = hmap.getProjection().fromScreenLocation(point);
        showLog("to latlng, latlng=" + newLatlng);
        VisibleRegion visibleRegion = hmap.getProjection().getVisibleRegion();
        showLog(visibleRegion.toString());
        //MOVE Camera to tapped locaiton
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newLatlng,
                DEFAULT_ZOOM);
        //hmap.moveCamera(update);
        hmap.animateCamera(update);

    }


    public void setTraffic(){
        if (trafficEnabled){
            hmap.setTrafficEnabled(false);
            trafficEnabled = false;
        }else{
            hmap.setTrafficEnabled(true);
            trafficEnabled = true;
        }
    }

    @SuppressLint("MissingPermission")
    public void findCurrentPlace() {
        if(null == query.getText()){
            return ;
        }
        if(null == placesClient){
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "need ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT);
            return;
        }

        FindCurrentPlaceRequest.Builder builder = FindCurrentPlaceRequest.builder(null);
        builder.setQuery(query.getText().toString());
        builder.setLimit(10);
        builder.setOffet(0);
        Log.d(TAG, "addMarker: start");
        Task<FindCurrentPlaceResponse> currentPlaceTask = placesClient.findCurrentPlace(builder.build());
        currentPlaceTask.addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {

            @Override
            public void onSuccess(FindCurrentPlaceResponse findCurrentPlaceResponse) {
                addMarker(findCurrentPlaceResponse.getPlaceLikelihoods());
            }
        });

        currentPlaceTask.addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "onSuccess() called with: error = [" + e.getMessage() + "]");

            }
        });
    }


    private void addMarker(List<PlaceLikelihood> places) {

        if(places.size() == 0){
            return;
        }
        // move camera to current postion
        LatLng cll = places.get(0).getPlace().getLatLng();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(cll,
                DEFAULT_ZOOM);
        //hmap.moveCamera(update);
        hmap.animateCamera(update);

        for (PlaceLikelihood place : places) {
            //add markers to the map
            Marker currentLocation = hmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(place.getPlace().getLatLng()).title("possible current Position").snippet("Current Position"));
            currentLocation.showInfoWindow();
        }
    }

    private void addMarkertoSearch(List<Place> places) {
        for (Place place : places) {
            //add markers to the map
            showLog(place.getName());
            Marker currentLocation = hmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(place.getLatLng()).title(place.getName()).snippet(place.getAddress()));
            currentLocation.showInfoWindow();
        }
    }

    public void searchByinput(View v) {
        if(null == placesClient){
            return;
        }
        com.huawei.hms.libraries.places.api.net.FindAutocompletePredictionsRequest.Builder builder =
                FindAutocompletePredictionsRequest.builder();
        builder.setLimit(10);
        builder.setOffet(0);
        builder.setQuery(query.getText().toString());
        // set location restriction
        LatLngBounds bounds = hmap.getProjection().getVisibleRegion().latLngBounds;
        builder.setLocationRestriction( RectangularBounds.newInstance(bounds));

        Log.d(TAG, "addMarker: start");
        Task<FindAutocompletePredictionsResponse> currentPlaceTask =
                placesClient.findAutocompletePredictions(builder.build());
        currentPlaceTask.addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {

            @Override
            public void onSuccess(FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                addMarkertoSearch(findAutocompletePredictionsResponse.getPlaces());
            }
        });

        currentPlaceTask.addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "onSuccess() called with: error = [" + e.getMessage() + "]");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        else if(item.getItemId()== R.id.turntraffic)
        {
            setTraffic();
        }

        else if(item.getItemId()== R.id.drawnCircle)
        {
            drawCircle();
        }

        else if(item.getItemId()== R.id.searchCurrentPlace)
        {
            findCurrentPlace();
            // do something
        }else if(item.getItemId() == R.id.styleMap){
            goToStyleMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void drawCircle() {
        if(null == hmap){
            return;
        }
        if (null != mCircle) {
            mCircle.remove();
        }
        CameraPosition cameraPosition = CameraPosition.builder().target(currentLatLng).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        hmap.moveCamera(cameraUpdate);
        mCircle = hmap
                .addCircle(new CircleOptions().center(currentLatLng).radius(500).fillColor(Color.GREEN));
    }

    private void goToStyleMap() {
        Intent i = new Intent(this, styleMapDemo.class);
        startActivity(i);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

}
