
package com.example.hmsdemo.cameraKit.video;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

class PermissionHelper {
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private static final String[] PERMISSIONS_ARRAY = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION};

    private static List<String> permissionsList = new ArrayList<>(PERMISSIONS_ARRAY.length);

    private PermissionHelper() {
    }

    public static boolean hasPermission(final Activity activity) {
        for (String permission : PERMISSIONS_ARRAY) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(final Activity activity) {
        for (String permission : PERMISSIONS_ARRAY) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
            REQUEST_CODE_ASK_PERMISSIONS);
    }
}
