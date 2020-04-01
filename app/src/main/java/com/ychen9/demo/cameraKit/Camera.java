package com.ychen9.demo.cameraKit;

import android.os.Bundle;

import com.ychen9.demo.BaseActivity;
import com.ychen9.demo.R;
import com.huawei.camera.camerakit.CameraKit;

public class Camera  extends BaseActivity {

    private static boolean isGetInstance = false;
    private String TAG = "CAMERAKIT";
    private CameraKit mCameraKit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraKit = CameraKit.getInstance(getApplicationContext());
        if (mCameraKit == null) {
            return;
        }

        // Query the camera list of the mobile phone. Currently, only the rear camera supports the super night mode.
        String[] cameraLists = mCameraKit.getCameraIdList();
// Query the modes supported by the current cameras.
        int[] modes = mCameraKit.getSupportedModes(cameraLists[0]);
// Create a mode.
        //
        // mCameraKit.createMode(cameraLists[0], mCurrentModeType, mModeStateCallback, mCameraKitHandler);
    }
}
