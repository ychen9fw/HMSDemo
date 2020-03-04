package com.example.hmsdemo.ml.menu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.example.hmsdemo.ml.classification.ImageClassificationAnalyseActivity;
import com.example.hmsdemo.ml.document.ImageDocumentAnalyseActivity;
import com.example.hmsdemo.ml.face.LiveFaceAnalyseActivity;
import com.example.hmsdemo.ml.face.StillFaceAnalyseActivity;
import com.example.hmsdemo.ml.landmark.ImageLandmarkAnalyseActivity;
import com.example.hmsdemo.ml.object.LiveObjectAnalyseActivity;
import com.example.hmsdemo.ml.text.ImageTextAnalyseActivity;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainMenu";

    private static final int PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_ml);
        setToolbar();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted, all good to go");
        } else {
            Log.d(TAG, "Permission not granted");
            requestPermission(Manifest.permission.INTERNET);
        }
    }

    public void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_ml_demo);
        }
    }

    private void requestPermission(String permissionType) {
        Log.d(TAG, "Requesting permission");
        final String[] permissions = new String[]{permissionType};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permissionType)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
            return;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Add Bussiness code
            return;
        }else{
            Log.d(TAG, "Failed to get permission");
        }
    }

    public void ImageClassificationOnClick(View view) {
        Intent intent = new Intent(this, ImageClassificationAnalyseActivity.class);
        startActivity(intent);
    }

    public void ImageDocumentAnalyseOnClick(View view) {
        Intent intent = new Intent(this, ImageDocumentAnalyseActivity.class);
        startActivity(intent);
    }

    public void LiveFaceAnalyseOnClick(View view) {
        Intent intent = new Intent(this, LiveFaceAnalyseActivity.class);
        startActivity(intent);
    }

    public void StillFaceAnalyseOnClick(View view) {
        Intent intent = new Intent(this, StillFaceAnalyseActivity.class);
        startActivity(intent);
    }

    public void ImageLandmarkAnalyseOnClick(View view) {
        Intent intent = new Intent(this, ImageLandmarkAnalyseActivity.class);
        startActivity(intent);
    }

    public void LiveObjectAnalyseOnClick(View view) {
        Intent intent = new Intent(this, LiveObjectAnalyseActivity.class);
        startActivity(intent);
    }

    public void ImageTextAnalyseOnClick(View view) {
        Intent intent = new Intent(this, ImageTextAnalyseActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
