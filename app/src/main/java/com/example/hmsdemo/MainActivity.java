package com.example.hmsdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.hmsdemo.map.MapDemo;
import com.example.hmsdemo.utils.PermissionManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private PermissionManager permissionManager;
    private final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isNetworkConnected()){
            Toast.makeText(this, "No Available Network. Please try again later", Toast.LENGTH_LONG).show();
            return;
        }

        permissionManager = new PermissionManager(this);
        findViewById(R.id.btn_HuaweiIDDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiLocationDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiPayDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiPushDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiAdsDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiMapDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiDriveDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiGameDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiAnalysisDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiAutheDemo).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        // Click on button
        switch (id) {
            case R.id.btn_HuaweiIDDemo:
                goToHuaweiIDDemo();
                break;
            case R.id.btn_HuaweiLocationDemo:
                checkPermission();
                break;
            case R.id.btn_HuaweiPayDemo:
                goToHuaweiPayDemo();
                break;
            case R.id.btn_HuaweiPushDemo:
                goToHuaweiPushDemo();
                break;
            case R.id.btn_HuaweiAdsDemo:
                goToHuaweiAdsDemo();
                break;

            case R.id.btn_HuaweiMapDemo:
                goToHuaweiMapDemo();
                break;

            case R.id.btn_HuaweiDriveDemo:
                goToHuaweiDriveDemo();
                break;

            case R.id.btn_HuaweiGameDemo:
                goToHuaweiGameDemo();
                break;

            case R.id.btn_HuaweiAnalysisDemo:
                goToHuaweiAnalysisDemo();
                break;
            case R.id.btn_HuaweiAutheDemo:
                goToHuaweiAutheDemo();
                break;
            default:
        }
    }

    private void goToHuaweiAutheDemo() {
        Intent intent = new Intent(this, AuthenticationDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiAnalysisDemo() {
        Intent intent = new Intent(this, AnalysisDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiAdsDemo() {
        Intent intent = new Intent(this, AdsDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiDriveDemo() {
        Intent intent = new Intent(this, DriveDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiGameDemo() {
        Intent intent = new Intent(this, GameDemo.class);
        startActivity(intent);
    }


    private void goToHuaweiMapDemo() {
        Intent intent = new Intent(this, MapDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiPushDemo() {
        Intent intent = new Intent(this, PushDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiPayDemo() {
        Intent intent = new Intent(this, PayDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiLocationDemo() {
        Intent intent = new Intent(this, LocationDemo.class);
        startActivity(intent);
    }

    private void goToHuaweiIDDemo() {
        Intent intent = new Intent(this, LoginDemo.class);
        startActivity(intent);
    }

    //Check and request user to give permission
    private void checkPermission(){
        permissionManager.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, new PermissionManager.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                showCameraRational();
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskAgain() {
                dialogForSettings("Permission Denied", "Please go to settings to enable location permission.");
            }

            @Override
            public void onPermissionGranted() {
                //permissionIsGranted = true;
                goToHuaweiLocationDemo();
            }
        });

    }


    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void showCameraRational() {
        new AlertDialog.Builder(this).setTitle("Permission Denied").setMessage("Location permission is essential for this app, without location permission, you are not able to get resource around you!")
                .setCancelable(false)
                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                        dialog.dismiss();
                    }
                }).show();

    }

    private void dialogForSettings(String title, String msg) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettings();
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permissionIsGranted = true;
                    goToHuaweiLocationDemo();
                } else {
                    // Permission was denied.......
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

    private void goToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + getPackageName());
        intent.setData(uri);
        startActivity(intent);
    }

}
