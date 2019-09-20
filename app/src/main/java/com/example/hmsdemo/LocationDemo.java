package com.example.hmsdemo;


import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.location.LocationAvailability;

import java.util.List;

public class LocationDemo extends BaseActivity implements View.OnClickListener{

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private String TAG = "Location DEMO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_demo);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        findViewById(R.id.btn_stopUpdatesLocation).setOnClickListener(this);
        findViewById(R.id.btn_updatesLocation).setOnClickListener(this);
        findViewById(R.id.btn_checkLocationAvailability).setOnClickListener(this);
        findViewById(R.id.btn_getLastKnownLocation).setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_location_demo);
        }
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (null == locationCallback) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        List<Location> locations = locationResult.getLocations();
                        if (!locations.isEmpty()) {
                            for (Location location : locations) {
                                showLog("onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                        + "," + location.getLatitude() + "," + location.getAccuracy());
                                Log.i(TAG,
                                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                                + "," + location.getLatitude() + "," + location.getAccuracy());
                            }
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        showLog("onLocationAvailability isLocationAvailable:" + flag);
                        Log.i(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }

    }

    @SuppressLint("MissingPermission")
    private void updatesLocation(){
        showLog("updating Location" );
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // check location session first before requesting location updates
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "check location settings success");
                            fusedLocationProviderClient
                                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            showLog("requestLocationUpdatesWithCallback onSuccess");
                                            Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            showLog("requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                            Log.e(TAG,
                                                    "requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        //call startResolutionForResult to ask for user's permission
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(LocationDemo.this, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        showLog("PendingIntent unable to execute request.");
                                        Log.e(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    // get last location
    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        showLog("getting current location" );
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                String curlatitude = String.valueOf(location.getLatitude());
                                String curlongitude = String.valueOf(location.getLongitude());
                                showLog("getLastLocation location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                        + "," + location.getLatitude() + "," + location.getAccuracy());
                            } else if (location == null) {
                                updatesLocation();
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

    @SuppressLint("MissingPermission")
    private void removeLocationUpdates(){
        showLog("remove location updates" );
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showLog("removeLocationUpdatesWithCallback onSuccess");
                            Log.i(TAG, "removeLocationUpdatesWithCallback onSuccess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            showLog("removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                            Log.e(TAG, "removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            showLog("removeLocationUpdatesWithCallback exception:" + e.getMessage());
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        //Remove the location updates
        removeLocationUpdates();
        super.onDestroy();
    }


    /**
     * get location availability
     */
    private void getLocationAvailability() {
        showLog("getting location availability" );
        try {
            fusedLocationProviderClient.getLocationAvailability()
                    .addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
                        @Override
                        public void onSuccess(LocationAvailability locationAvailability) {
                            if (locationAvailability != null) {
                                showLog("getLocationAvailability onSuccess:" + locationAvailability.toString());
                                Log.i(TAG, "getLocationAvailability onSuccess:" + locationAvailability.toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            showLog("getLocationAvailability onFailure:" + e.getMessage());
                            Log.e(TAG, "getLocationAvailability onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            showLog("getLocationAvailability exception:" + e.getMessage());
            Log.e(TAG, "getLocationAvailability exception:" + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_checkLocationAvailability:
                getLocationAvailability();
                break;
            case R.id.btn_getLastKnownLocation:
                getCurrentLocation();
                break;
            case R.id.btn_updatesLocation:
                updatesLocation();
                break;
            case R.id.btn_stopUpdatesLocation:
                removeLocationUpdates();
                break;
            default:
        }
    }

}
