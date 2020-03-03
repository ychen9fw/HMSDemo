/* CameraKitPortraitCaptureActivity.java */

package com.example.hmsdemo.cameraKit.portrait;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.huawei.camera.camerakit.ActionDataCallback;
import com.huawei.camera.camerakit.ActionStateCallback;
import com.huawei.camera.camerakit.CameraKit;
import com.huawei.camera.camerakit.Metadata;
import com.huawei.camera.camerakit.Mode;
import com.huawei.camera.camerakit.ModeCharacteristics;
import com.huawei.camera.camerakit.ModeConfig;
import com.huawei.camera.camerakit.ModeStateCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Demo Activity
 *
 */
public class CameraKitPortraitCaptureActivity extends BaseActivity {
    private static final String TAG = CameraKit.class.getSimpleName();

    private static final String PIVOT = " ";

    /**
     * View for preview
     */
    private AutoFitTextureView mTextureView;

    /**
     * Button for capture
     */
    private Button mButtonCaptureImage;

    /**
     * Preview size
     */
    private Size mPreviewSize;

    /**
     * Capture size
     */
    private Size mCaptureSize;

    /**
     * Capture jpeg file
     */
    private File mFile;

    /**
     * CameraKit instance
     */
    private CameraKit mCameraKit;

    /**
     * Current mode type
     */
    private @Mode.Type int mCurrentModeType = Mode.Type.PORTRAIT_MODE;

    /**
     * Current mode object
     */
    private Mode mMode;

    /**
     * Mode characteristics
     */
    private ModeCharacteristics mModeCharacteristics;

    /**
     * Mode config builder
     */
    private ModeConfig.Builder modeConfigBuilder;

    /**
     * Work thread for time consumed task
     */
    private HandlerThread mCameraKitThread;

    /**
     * Handler correspond to mCameraKitThread
     */
    private Handler mCameraKitHandler;

    /**
     * Lock for camera device
     */
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
            runOnUiThread(() -> {
                initSlenderSpinner();
                initSkinColorSpinner();
                initBodySpinner();
            });
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
        // Query camera id list
        String[] cameraLists = mCameraKit.getCameraIdList();
        if ((cameraLists != null) && (cameraLists.length > 0)) {
            Log.i(TAG, "Try to use camera with id " + cameraLists[1]);
            // Query supported modes of this device
            int[] modes = mCameraKit.getSupportedModes(cameraLists[0]);
            if (!Arrays.stream(modes).anyMatch((i) -> i == mCurrentModeType)) {
                Log.w(TAG, "Current mode is not supported in this device!");
                return;
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
        // Query supported preview size
        List<Size> previewSizes = mModeCharacteristics.getSupportedPreviewSizes(SurfaceTexture.class);
        // Query supported capture size
        List<Size> captureSizes = mModeCharacteristics.getSupportedCaptureSizes(ImageFormat.JPEG);
        Log.d(TAG, "configMode: captureSizes = " + captureSizes.size() + ";previewSizes=" + previewSizes.size());
        // Use the first one or default 4000x3000
        mCaptureSize = captureSizes.stream().findFirst().orElse(new Size(4000, 3000));
        // Use the same ratio with preview
        mPreviewSize = previewSizes.stream().filter((size) -> Math.abs((1.0f * size.getHeight() / size.getWidth()) - (1.0f * mCaptureSize.getHeight() / mCaptureSize.getWidth())) < 0.01).findFirst().get();
        Log.i(TAG, "configMode: mCaptureSize = " + mCaptureSize + ";mPreviewSize=" + mPreviewSize);
        // Update view
        runOnUiThread(() -> mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth()));
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) {
            Log.e(TAG, "configMode: texture=null!");
            return;
        }
        // Set buffer size of view
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        // Get surface of texture
        Surface surface = new Surface(texture);
        // Add preview and capture parameters to config builder
        modeConfigBuilder.addPreviewSurface(surface).addCaptureImage(mCaptureSize, ImageFormat.JPEG);
        // Set callback for config builder
        modeConfigBuilder.setDataCallback(actionDataCallback, mCameraKitHandler);
        modeConfigBuilder.setStateCallback(actionStateCallback, mCameraKitHandler);
        // Configure mode
        mMode.configure();
        Log.i(TAG, "configMode end");
    }

    private void captureImage() {
        Log.i(TAG, "captureImage begin");
        if (mMode != null) {
            mMode.setImageRotation(90);
            // Default jpeg file path
            mFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + "pic.jpg");
            // Take picture
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
        setContentView(R.layout.activity_camera_portrait);
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

    private void initSlenderSpinner() {
        initSpinner(R.id.slenderSpinner,
            intToList(mModeCharacteristics.getSupportedBeauty(Metadata.BeautyType.HW_BEAUTY_FACE_SLENDER),
                R.string.slender),
            new SpinnerOperation() {
                @Override
                public void doOperation(String text) {
                    try {
                        mMode.setBeauty(Metadata.BeautyType.HW_BEAUTY_FACE_SLENDER,
                            Integer.valueOf(text.split(PIVOT)[1]));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "NumberFormatException initSlenderSpinner text: " + text);
                    }
                }
            });
    }

    private void initSkinColorSpinner() {
        initSpinner(R.id.skinColorSpinner,
            intToList(mModeCharacteristics.getSupportedBeauty(Metadata.BeautyType.HW_BEAUTY_SKIN_COLOR),
                R.string.skinColor),
            new SpinnerOperation() {
                @Override
                public void doOperation(String text) {
                    try {
                        mMode.setBeauty(Metadata.BeautyType.HW_BEAUTY_SKIN_COLOR,
                            Integer.valueOf(text.split(PIVOT)[1]));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "initSkinColorSpinner text:" + text);
                    }
                }
            });
    }

    private void initBodySpinner() {
        initSpinner(R.id.bodySpinner,
            intToList(mModeCharacteristics.getSupportedBeauty(Metadata.BeautyType.HW_BEAUTY_BODY_SHAPING),
                R.string.body),
            new SpinnerOperation() {
                @Override
                public void doOperation(String text) {
                    try {
                        mMode.setBeauty(Metadata.BeautyType.HW_BEAUTY_BODY_SHAPING,
                            Integer.valueOf(text.split(PIVOT)[1]));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "NumberFormatException text: " + text);
                    }
                }
            });
    }

    private void initSpinner(int resId, List<String> list, final SpinnerOperation operation) {
        final Spinner spinner = findViewById(resId);
        spinner.setVisibility(View.VISIBLE);
        if (list.size() == 0) {
            spinner.setVisibility(View.GONE);
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.itemText, list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = spinner.getItemAtPosition(position).toString();
                mCameraKitHandler.post(() -> {
                    if (mMode != null) {
                        operation.doOperation(text);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private List<String> intToList(int[] values, int id) {
        List<String> lists = new ArrayList<>(0);
        if ((values == null) || (values.length == 0)) {
            Log.d(TAG, "getIntList1, values is null");
            return lists;
        }
        for (int mode : values) {
            lists.add(getString(id) + PIVOT + mode);
        }
        return lists;
    }
}