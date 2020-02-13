package com.example.hmsdemo.Maps;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;
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
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.VisibleRegion;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.site.api.model.TextSearchRequest;
import com.huawei.hms.site.api.model.TextSearchResponse;

import java.util.ArrayList;
import java.util.List;

public class GHLocation extends BaseActivity implements View.OnClickListener, OnMapReadyCallback, HuaweiMap.OnMapClickListener{

    private BaseLocation locationService;
    private Location currentLocation;
    private SearchService searchService;
    TextView resultTextView;
    EditText queryInput;

    private static final String TAG = "MapViewDemoActivity";
    private HuaweiMap hmap;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static float DEFAULT_ZOOM = (float) 11.0;
    private LatLng currentLatLng;
    private boolean trafficEnabled;
    private Circle mCircle;
    private ArrayList<Site>  sitList = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghlocation);
        findViewById(R.id.btn_search).setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        trafficEnabled = false;
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_location_demo);
        }

        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        //Use Interface to Judje whether Mobile Phone Supports Huawei MoBile Service,If supported,the result will be return to SUCCESS
        int hmsResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);

        //If both services are supported, GMS is used
        if (gmsResult == ConnectionResult.SUCCESS) {
            //Initialized as GMS PUSH functional class
            locationService = new GMSLocation(this);
            tag = "GMS:";
        } else if (hmsResult == com.huawei.hms.api.ConnectionResult.SUCCESS) {
            //Initialized as HMS PUSH functional class
            locationService = new HMSLocation(this);
            tag = "HMS:";
        } else {//If neither service supports, hide all buttons
            return;
        }

        mMapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        searchService = SearchServiceFactory.create(this);

        queryInput = findViewById(R.id.edit_text_text_search_query);

        locationService.setLocationRequest();
        locationService.setUpLocationCallBack();
        locationService.setonLocationCallBack(new BaseLocation.onLocationCallBack() {
            @Override
            public void callBack(Location location) {
                showLog("current Location is:" + location.getLongitude()
                        + "," + location.getLatitude() + "," + location.getAccuracy());
                currentLocation = location;
            }
        });
        getCurrentLocation();
    }

    public void searchPlace(){
        TextSearchRequest textSearchRequest = new TextSearchRequest();
        textSearchRequest.setQuery(queryInput.getText().toString());
        Coordinate coordinate = new Coordinate(currentLocation.getLatitude(), currentLocation.getLongitude());
        textSearchRequest.setLocation(coordinate);
        textSearchRequest.setRadius(5000);
        searchService.textSearch(textSearchRequest, new SearchResultListener<TextSearchResponse>() {
            @Override
            public void onSearchResult(TextSearchResponse textSearchResponse) {

                StringBuilder response = new StringBuilder("\n");
                response.append("success\n");
                for (Site site : textSearchResponse.getSites()) {
                    sitList.add(site);
                }
                addMarkertoSearch(sitList);
            }

            @Override
            public void onSearchError(SearchStatus searchStatus) {
                showLog( "onSearchError is: " + searchStatus.getErrorCode());
            }
        });
    }

    private void addMarker(List<Site> sitList) {

        if(sitList.size() == 0){
            return;
        }
        // move camera to current postion
        LatLng cll = new LatLng (sitList.get(0).getLocation().getLat(),sitList.get(0).getLocation().getLng()) ;
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(cll,
                DEFAULT_ZOOM);
        //hmap.moveCamera(update);
        hmap.animateCamera(update);

        for (Site site : sitList) {
            //add markers to the map
            LatLng tempClt = new LatLng (site.getLocation().getLat(),site.getLocation().getLng()) ;
            Marker currentLocation = hmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(tempClt).title("possible current Position").snippet("Current Position"));
            currentLocation.showInfoWindow();
        }
    }

    private void addMarkertoSearch(List<Site> sitList) {
        for (Site site : sitList) {
            //add markers to the map
            showLog(site.getName());
            LatLng tempClt = new LatLng (site.getLocation().getLat(),site.getLocation().getLng()) ;
            Marker currentLocation = hmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(tempClt).title(site.getName()).snippet(site.getFormatAddress()));
            currentLocation.showInfoWindow();
        }
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


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(HuaweiMap map) {

        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        Log.d(TAG, "onMapReady: ");

        hmap = map;
        hmap.setMyLocationEnabled(true);
        hmap.getUiSettings().setZoomControlsEnabled(true);
        hmap.getUiSettings().setMyLocationButtonEnabled(true);
        hmap.getUiSettings().setCompassEnabled(true);
        hmap.getUiSettings().setAllGesturesEnabled(true);
        LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 19);
        hmap.animateCamera(yourLocation);
        // hmap.setMapStyle();

        //Set a tap event listener for a map.
        hmap.setOnMapClickListener(this);

        // get current locaiton
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_search:
                searchPlace();
                break;
            default:
        }
    }

    private void getLocationAvailability() {
        locationService.getLocationAvailability();
    }


    private void updatesLocation() {
        locationService.updatesLocation();
    }


    private void getCurrentLocation() {
        locationService.getCurrentLocation();
    }

    private void removeLocationUpdates() {
        locationService.removeLocationUpdates();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

}
