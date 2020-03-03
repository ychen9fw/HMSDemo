/* CameraKitHdrCaptureActivity.java */

package com.example.hmsdemo.cameraKit.hdr;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.huawei.camera.camerakit.ActionDataCallback;
import com.huawei.camera.camerakit.ActionStateCallback;
import com.huawei.camera.camerakit.CameraKit;
import com.huawei.camera.camerakit.Mode;
import com.huawei.camera.camerakit.ModeCharacteristics;
import com.huawei.camera.camerakit.ModeConfig;
import com.huawei.camera.camerakit.ModeStateCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * CameraKitHdrCaptureActivity
 *
 */
public class CameraKitHdrCaptureActivity extends BaseActivity {
    private static final String TAG = CameraKit.class.getSimpleName();

    private AutoFitTextureView mTextureView;

    private Button mButtonCaptureImage;

    private Size mPreviewSize;

    private Size mCaptureSize;

    private File mFile;

    private CameraKit mCameraKit;

    private @Mode.Type int mCurrentModeType = Mode.Type.HDR_MODE;

    private Mode mMode;

    private ModeCharacteristics mModeCharacteristics;

    private ModeConfig.Builder modeConfigBuilder;

    private HandlerThread mCameraKitThread;

    private Handler mCameraKitHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    mCameraKitHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            createMode();
                        }
                    });
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {
                }
            };

    private final ActionDataCallback actionDataCallback = new ActionDataCallback() {
        @Override
        public void onImageAvailable(Mode mode, @Type int type, Image image) {
            Log.d(TAG, "onImageAvailable: save img");
            switch (type) {
                case Type.TAKE_PICTURE: {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(mFile);
                        output.write(bytes);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException when write in run");
                    } finally {
                        image.close();
                        if (output != null) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Log.e(TAG, "IOException when close in run");
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    private final ActionStateCallback actionStateCallback = new ActionStateCallback() {
        @Override
        public void onPreview(Mode mode, int state, PreviewResult result) {
            if (state == PreviewResult.State.PREVIEW_STARTED) {
                Log.i(TAG, "onPreview Started");
            }
        }

        @Override
        public void onTakePicture(Mode mode, int state, TakePictureResult result) {
            switch (state) {
                case TakePictureResult.State.CAPTURE_STARTED:
                    Log.d(TAG, "onState: STATE_CAPTURE_STARTED");
                    break;
                case TakePictureResult.State.CAPTURE_COMPLETED:
                    Log.d(TAG, "onState: STATE_CAPTURE_COMPLETED");
                    showToast("take picture success! file=" + mFile);
                    break;
                default:
                    break;
            }
        }
    };

    private final ModeStateCallback mModeStateCallback = new ModeStateCallback() {
        @Override
        public void onCreated(Mode mode) {
            Log.d(TAG, "mModeStateCallback onModeOpened: ");
            mCameraOpenCloseLock.release();
            mMode = mode;
            mModeCharacteristics = mode.getModeCharacteristics();
            modeConfigBuilder = mMode.getModeConfigBuilder();
            configMode();
        }

        @Override
        public void onCreateFailed(String cameraId, int modeType, int errorCode) {
            Log.d(TAG,
                    "mModeStateCallback onCreateFailed with errorCode: " + errorCode + " and with cameraId: " + cameraId);
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onConfigured(Mode mode) {
            Log.d(TAG, "mModeStateCallback onModeActivated : ");
            mMode.startPreview();
        }

        @Override
        public void onConfigureFailed(Mode mode, int errorCode) {
            Log.d(TAG, "mModeStateCallback onConfigureFailed with cameraId: " + mode.getCameraId());
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onFatalError(Mode mode, int errorCode) {
            Log.d(TAG, "mModeStateCallback onFatalError with errorCode: " + errorCode + " and with cameraId: "
                    + mode.getCameraId());
            mCameraOpenCloseLock.release();
            finish();
        }

        @Override
        public void onReleased(Mode mode) {
            Log.d(TAG, "mModeStateCallback onModeReleased: ");
            mCameraOpenCloseLock.release();
        }
    };

    private void createMode() {
        Log.i(TAG, "createMode begin");
        mCameraKit = CameraKit.getInstance(getApplicationContext());
        if (mCameraKit == null) {
            Log.e(TAG, "This device does not support CameraKit！");
            showToast("CameraKit not exist or version not compatible");
            return;
        }
        String[] cameraLists = mCameraKit.getCameraIdList();
        if ((cameraLists != null) && (cameraLists.length > 0)) {
            Log.i(TAG, "Try to use camera with id " + cameraLists[0]);
            int[] modes = mCameraKit.getSupportedModes(cameraLists[0]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!Arrays.stream(modes).anyMatch((i) -> i == mCurrentModeType)) {
                    Log.w(TAG, "Current mode is not supported in this device!");
                    return;
                }
            }
            try {
                if (!mCameraOpenCloseLock.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                mCameraKit.createMode(cameraLists[0], mCurrentModeType, mModeStateCallback, mCameraKitHandler);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
        }
        Log.i(TAG, "createMode end");
    }

    private void configMode() {
        Log.i(TAG, "configMode begin");
        List<Size> previewSizes = mModeCharacteristics.getSupportedPreviewSizes(SurfaceTexture.class);
        List<Size> captureSizes = mModeCharacteristics.getSupportedCaptureSizes(ImageFormat.JPEG);
        Log.d(TAG, "configMode: captureSizes = " + captureSizes.size() + ";previewSizes=" + previewSizes.size());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mCaptureSize = captureSizes.stream().findFirst().orElse(new Size(4000, 3000));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPreviewSize = previewSizes.stream().filter((size) -> Math.abs((1.0f * size.getHeight() / size.getWidth()) - (1.0f * mCaptureSize.getHeight() / mCaptureSize.getWidth())) < 0.01).findFirst().get();
        }
        Log.i(TAG, "configMode: mCaptureSize = " + mCaptureSize + ";mPreviewSize=" + mPreviewSize);
        runOnUiThread(() -> mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth()));
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) {
            Log.e(TAG, "configMode: texture=null!");
            return;
        }
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        modeConfigBuilder.addPreviewSurface(surface).addCaptureImage(mCaptureSize, ImageFormat.JPEG);
        modeConfigBuilder.setDataCallback(actionDataCallback, mCameraKitHandler);
        modeConfigBuilder.setStateCallback(actionStateCallback, mCameraKitHandler);
        mMode.configure();
        Log.i(TAG, "configMode end");
    }

    private void captureImage() {
        Log.i(TAG, "captureImage begin");
        if (mMode != null) {
            mMode.setImageRotation(90);
            mFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + "pic.jpg");
            mMode.takePicture();
        }
        Log.i(TAG, "captureImage end");
    }

    private void showToast(final String text) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_hdr);
        mButtonCaptureImage = findViewById(R.id.capture_image);
        mButtonCaptureImage.setOnClickListener((v) -> captureImage());
        mTextureView = findViewById(R.id.texture);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (!PermissionHelper.hasPermission(this)) {
            PermissionHelper.requestPermission(this);
            return;
        }
        startBackgroundThread();
        if (mTextureView != null) {
            if (mTextureView.isAvailable()) {
                mCameraKitHandler.post(() -> createMode());
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        if (mMode != null) {
            mCameraKitHandler.post(() -> {
                try {
                    mCameraOpenCloseLock.acquire();
                    mMode.release();
                    mMode = null;
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
                } finally {
                    Log.d(TAG, "closeMode:");
                    mCameraOpenCloseLock.release();
                }
            });
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        stopBackgroundThread();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        if (!PermissionHelper.hasPermission(this)) {
            Toast.makeText(this, "This application needs camera permission.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread");
        if (mCameraKitThread == null) {
            mCameraKitThread = new HandlerThread("CameraBackground");
            mCameraKitThread.start();
            mCameraKitHandler = new Handler(mCameraKitThread.getLooper());
            Log.d(TAG, "startBackgroundTThread: mCameraKitThread.getThreadId()=" + mCameraKitThread.getThreadId());
        }
    }

    private void stopBackgroundThread() {
        Log.d(TAG, "stopBackgroundThread");
        if (mCameraKitThread != null) {
            mCameraKitThread.quitSafely();
            try {
                mCameraKitThread.join();
                mCameraKitThread = null;
                mCameraKitHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException in stopBackgroundThread " + e.getMessage());
            }
        }
    }
}