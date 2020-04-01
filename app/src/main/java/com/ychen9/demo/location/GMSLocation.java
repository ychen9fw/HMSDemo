package com.ychen9.demo.location;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.ychen9.demo.BaseActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import androidx.annotation.NonNull;

public class GMSLocation extends BaseLocation{

    private static final String TAG = "GMS Location";

    private static LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;

    public GMSLocation(BaseActivity baseActivity){
        this.baseActivity = baseActivity;
    }


    @Override
    public void setLocationRequest(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(baseActivity);
        settingsClient = LocationServices.getSettingsClient(baseActivity);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void setUpLocationCallBack(){
        if (null == locationCallback) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        List<android.location.Location> locations = locationResult.getLocations();
                        if (!locations.isEmpty()) {
                            for (Location location : locations) {
                                baseActivity.showLog("onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                        + "," + location.getLatitude() + "," + location.getAccuracy());
                                callBack.callBack(location);
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
                        baseActivity.showLog("onLocationAvailability isLocationAvailable:" + flag);
                        //Log.i(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }

    }

    @Override
    @SuppressLint("MissingPermission")
    public void updatesLocation(){
        baseActivity.showLog("updating Location" );
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
                                            baseActivity.showLog("requestLocationUpdatesWithCallback onSuccess");
                                            Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            baseActivity.showLog("requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
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
                                        rae.startResolutionForResult(baseActivity, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        //showLog("PendingIntent unable to execute request.");
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

    @Override
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(){
        baseActivity.showLog("getting current location" );
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                baseActivity.showLog("getLastLocation success");
                                callBack.callBack(location);
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

    @Override
    @SuppressLint("MissingPermission")
    public void removeLocationUpdates() {
        baseActivity.showLog("remove location updates");
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            baseActivity.showLog("removeLocationUpdatesWithCallback onSuccess");
                            Log.i(TAG, "removeLocationUpdatesWithCallback onSuccess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            baseActivity.showLog("removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                            Log.e(TAG, "removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            baseActivity.showLog("removeLocationUpdatesWithCallback exception:" + e.getMessage());
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void getLocationAvailability() {
        baseActivity.showLog("getting location availability" );
        try {
            fusedLocationProviderClient.getLocationAvailability()
                    .addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
                        @Override
                        public void onSuccess(LocationAvailability locationAvailability) {
                            if (locationAvailability != null) {
                                baseActivity.showLog("getLocationAvailability onSuccess:" + locationAvailability.toString());
                                Log.i(TAG, "getLocationAvailability onSuccess:" + locationAvailability.toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            baseActivity.showLog("getLocationAvailability onFailure:" + e.getMessage());
                            Log.e(TAG, "getLocationAvailability onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            baseActivity.showLog("getLocationAvailability exception:" + e.getMessage());
            Log.e(TAG, "getLocationAvailability exception:" + e.getMessage());
        }
    }
}
