package com.example.hmsdemo.location;

import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;

public class GHLocation extends BaseActivity implements View.OnClickListener {

    private BaseLocation locationService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghlocation);

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

        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        //Use Interface to Judje whether Mobile Phone Supports Huawei MoBile Service,If supported,the result will be return to SUCCESS
        int hmsResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);

        //If both services are supported, GMS is used
        if(gmsResult == ConnectionResult.SUCCESS){
            //Initialized as GMS PUSH functional class
            locationService = new GMSLocation(this);
            tag = "GMS:";
        }else if(hmsResult == com.huawei.hms.api.ConnectionResult.SUCCESS){
            //Initialized as HMS PUSH functional class
            locationService = new HMSLocation(this);
            tag = "HMS:";
        }else {//If neither service supports, hide all buttons
            return;
        }
        locationService.setLocationRequest();
        locationService.setUpLocationCallBack();
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

    private void getLocationAvailability() {
        locationService.getLocationAvailability();
    }


    private void updatesLocation() {
        locationService.updatesLocation();
    }


    private void getCurrentLocation() {
        locationService.setonLocationCallBack(new BaseLocation.onLocationCallBack() {
            @Override
            public void callBack(Location location) {
                showLog("location is " + location.getLatitude() + ", " + location.getLongitude() );
            }
        });
    }

    private void removeLocationUpdates() {
        locationService.removeLocationUpdates();
    }
}
