package com.example.hmsdemo.cameraKit.video;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.ConditionVariable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.huawei.camera.camerakit.CameraKit;
import com.huawei.camera.camerakit.Metadata;
import com.huawei.camera.camerakit.Mode;
import com.huawei.camera.camerakit.ModeCharacteristics;
import com.huawei.camera.camerakit.ModeConfig;
import com.huawei.camera.camerakit.ModeStateCallback;
import com.huawei.camera.camerakit.RequestKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

/**
 * The camera activity
 *
 * @since 2019/09/02
 */
public class CameraKitBasicVideoActivity extends BaseActivity {
    private static final String TAG = CameraKitBasicVideoActivity.class.getSimpleName();

    private static final int OPEN_CAEMERA_TIME_OUT = 2500;

    private static final long PREVIEW_SURFACE_READY_TIMEOUT = 5000L;

    private static final String PIVOT = " ";

    private static final int VIDEO_ENCODING_BIT_RATE = 10000000;

    private static final int VIDEO_FRAME_RATE = 30;

    private final ConditionVariable mPreviewSurfaceChangedDone = new ConditionVariable();

    private AutoFitTextureView mTextureView;

    private Size mPreviewSize;

    private Surface mVideoSurface;

    private CameraKitHelper.RecordState mRecordState;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private @Mode.Type int mCurrentModeType = Mode.Type.VIDEO_MODE;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private Semaphore mStartStopRecordLock = new Semaphore(1);

    private CameraKit mCameraKit;

    private ModeCharacteristics mModeCharacteristics;

    private int mSensorOrientation;

    private MediaRecorder mMediaRecorder;

    private String mVideoFile = "";

    private Size mRecordSize;

    private Button mButtonVideo;

    private Button mButtonRecordPause;

    private Mode mMode;

    private ConstraintLayout statusTextBox;
    private TextView statusText;

    private final View.OnClickListener mVideoPauseAndResumeListener = new View.OnClickListener() {
        @Override
        public void onClick(View vi) {
            Log.d(TAG, "onClick: video");
            mBackgroundHandler.post(() -> {
                if (mRecordState == CameraKitHelper.RecordState.RECORDING) {
                    pauseRecord();
                } else if (mRecordState == CameraKitHelper.RecordState.PAUSED) {
                    resumeRecord();
                } else {
                    Log.d(TAG, "not in idele or RECORDING state");
                }
            });
        }
    };
    private ModeConfig.Builder modeConfigBuilder;
    private boolean mIsFirstRecord = true;
    private final ModeStateCallback mModeStateCallback = new ModeStateCallback() {
        @Override
        public void onCreated(Mode mode) {
            mCameraOpenCloseLock.release();
            mMode = mode;
            modeConfigBuilder = mMode.getModeConfigBuilder();
            activeVideoModePreview();
        }

        @Override
        public void onCreateFailed(String cameraId, int modeType, int errorCode) {
            Log.d(TAG,
                "mModeStateCallback onCreateFailed with errorCode: " + errorCode + " and with cameraId: " + cameraId);
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onConfigured(Mode mode) {
            mMode.startPreview();
            runOnUiThread(() -> {
                if ((mTextureView == null) || (mPreviewSize == null)) {
                    return;
                }
                CameraKitHelper.configureTransform(CameraKitBasicVideoActivity.this, mTextureView, mPreviewSize,
                    new Size(mTextureView.getWidth(), mTextureView.getHeight()));
            });
            CameraKitBasicVideoActivity.this.runOnUiThread(() -> {
                initAiMovieSpinner();
                mButtonVideo.setVisibility(View.VISIBLE);
                mButtonVideo.setEnabled(true);
            });
        }

        @Override
        public void onConfigureFailed(Mode mode, int errorCode) {
            Log.d(TAG, "mModeStateCallback onConfigureFailed with cameraId: " + mode.getCameraId());
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onReleased(Mode mode) {
            Log.d(TAG, "mModeStateCallback onModeReleased: ");
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onFatalError(Mode mode, int errorCode) {
            Log.d(TAG, "mModeStateCallback onFatalError with errorCode: " + errorCode + " and with cameraId: "
                + mode.getCameraId());
            mCameraOpenCloseLock.release();
            finish();
        }
    };
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener =
        new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: " + new Size(width, height));
                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamerakit();
                    }
                });
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: " + new Size(width, height) + " PreviewSize:" + mPreviewSize);
                if ((mTextureView == null) || (mPreviewSize == null)) {
                    return;
                }
                CameraKitHelper.configureTransform(CameraKitBasicVideoActivity.this, mTextureView, mPreviewSize,
                    new Size(width, height));
                mPreviewSurfaceChangedDone.open();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }
        };
    private final View.OnClickListener mVideoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View vi) {
            mBackgroundHandler.post(() -> {
                if (mRecordState == CameraKitHelper.RecordState.IDLE) {
                    startRecord();
                } else if (mRecordState == CameraKitHelper.RecordState.RECORDING) {
                    stopRecord();
                } else {
                    Log.d(TAG, "No new command issued!");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_video);
        mButtonVideo = findViewById(R.id.video);
        mButtonVideo.setOnClickListener(mVideoClickListener);
        mButtonRecordPause = findViewById(R.id.recordPause);
        mButtonRecordPause.setOnClickListener(mVideoPauseAndResumeListener);
        mTextureView = findViewById(R.id.texture);
        statusTextBox = findViewById(R.id.status_text_box);
        statusText = findViewById(R.id.status_text);
        CameraKitHelper.checkImageDirectoryExists();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!PermissionHelper.hasPermission(this)) {
            PermissionHelper.requestPermission(this);
            return;
        }
        startBackgroundThread();
        if (mTextureView != null) {
            Log.d(TAG, "onResume: setSurfaceTextureListener: ");
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            if (mTextureView.isAvailable()) {
                Log.d(TAG, "onResume startCamerakit");
                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamerakit();
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        if (mMode != null) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCameraOpenCloseLock.acquire();
                        mMode.release();
                        mMode = null;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
                    } finally {
                        Log.d(TAG, "closeMode:------ ");
                        mCameraOpenCloseLock.release();
                    }
                    releaseMediaRecorder();
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

    private void startCamerakit() {
        mCameraKit = CameraKit.getInstance(getApplicationContext());
        if (mCameraKit == null) {
            Log.e(TAG, "startCamerakit: failed! this device does't not support camerakit");
            showToast("CameraKit not exist or version not compatible");
            return;
        }
        String[] cameraLists = mCameraKit.getCameraIdList();
        if ((cameraLists != null) && (cameraLists.length > 0)) {
            Log.d(TAG, "openCamera: cameraId=" + cameraLists[0]);
            try {
                if (!mCameraOpenCloseLock.tryAcquire(OPEN_CAEMERA_TIME_OUT, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                mCameraKit.createMode(cameraLists[0], mCurrentModeType, mModeStateCallback, mBackgroundHandler);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
        }
        mMediaRecorder = new MediaRecorder();
        mModeCharacteristics = mCameraKit.getModeCharacteristics(cameraLists[0], mCurrentModeType);
        mSensorOrientation = mModeCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
    }

    private void showToast(final String text) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        });
    }

    private void activeVideoModePreview() {

        try{
            List<Size> previewSizes = mModeCharacteristics.getSupportedPreviewSizes(SurfaceTexture.class);
            List<Size> recordSizes = getSupportedVideoSizes();
            for(Size item: previewSizes){
                Log.d(TAG, "activeVideoModePreview >> Supported Preview Size >> "+ item.getHeight() + ", "+item.getWidth());
            }
            for(Size item: recordSizes){
                Log.d(TAG, "activeVideoModePreview >> Supported Record Size >> "+ item.getHeight() + ", "+item.getWidth());
            }
            mRecordSize = Collections.max(recordSizes, new CameraKitHelper.CompareSizesByArea());
            Size previewSize = CameraKitHelper.getOptimalVideoPreviewSize(this, mRecordSize, previewSizes);
            if (previewSize == null) {
                Log.d(TAG, "activeVideoModePreview: preview size is null");
                return;
            }else{
                Log.d(TAG, "activeVideoModePreview >> Preview Size >> "+ previewSize.getHeight() + ", "+previewSize.getWidth());
            }
            runOnUiThread(() -> {
                Log.d(TAG, "activeVideoModePreview >> " + previewSize.getHeight() + ", "+previewSize.getWidth());
                mTextureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            });
            if ((mPreviewSize == null) || ((previewSize.getHeight() != mPreviewSize.getHeight())
                    || (previewSize.getWidth() != mPreviewSize.getWidth()))) {
                Log.e(TAG, "activeVideoModePreview: mPreviewSurfaceChangedDone start:" + previewSize);
                mPreviewSize = previewSize;
                mPreviewSurfaceChangedDone.block(PREVIEW_SURFACE_READY_TIMEOUT);
            } else {
                mPreviewSize = previewSize;
            }
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "activeVideoModePreview: texture=null!");
                return;
            }
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            modeConfigBuilder.addPreviewSurface(surface);
            mVideoSurface = MediaCodec.createPersistentInputSurface();
            setUpMediaRecorder(mRecordSize, mVideoSurface);
            mRecordState = CameraKitHelper.RecordState.IDLE;
            mIsFirstRecord = true;
            modeConfigBuilder.addVideoSurface(mVideoSurface);
            runOnUiThread(() -> {
                statusTextBox.setVisibility(View.GONE);
            });

            mMode.configure();
        }catch(Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                statusText.setText(e.toString());
                statusTextBox.setVisibility(View.VISIBLE);
            });
        }


    }

    private List<Size> getSupportedVideoSizes() {
        Map<Integer, List<Size>> videoSizes = mModeCharacteristics.getSupportedVideoSizes(MediaRecorder.class);
        if ((videoSizes != null) && videoSizes.containsKey(Metadata.FpsRange.HW_FPS_30)) {
            return videoSizes.get(Metadata.FpsRange.HW_FPS_30);
        }
        return new ArrayList<>(0);
    }

    private void setUpMediaRecorder(Size size, Surface surface) {
        mMediaRecorder.reset();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mVideoFile = CameraKitHelper.getVideoName();
        mMediaRecorder.setOutputFile(mVideoFile);
        mMediaRecorder.setVideoEncodingBitRate(VIDEO_ENCODING_BIT_RATE);
        mMediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        mMediaRecorder.setVideoSize(size.getWidth(), size.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        mMediaRecorder.setOrientationHint(CameraKitHelper.getOrientation(mSensorOrientation, rotation));
        mMediaRecorder.setInputSurface(surface);
        try {
            mMediaRecorder.prepare();
            Log.d(TAG, "mMediaRecorder prepare done!");
        } catch (IOException e) {
            Log.e(TAG, "mMediaRecorder prepare ioe exception " + e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "mMediaRecorder prepare state error");
        }
    }

    /**
     * start background thread
     */
    private void startBackgroundThread() {
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            Log.d(TAG, "startBackgroundTThread: mBackgroundThread.getThreadId()=" + mBackgroundThread.getThreadId());
        }
    }

    /**
     * stop background thread
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException in stopBackgroundThread " + e.getMessage());
            }
        }
    }

    private void initAiMovieSpinner() {
        Byte[] ranges = new Byte[0];
        List<CaptureRequest.Key<?>> parameters = mModeCharacteristics.getSupportedParameters();
        if ((parameters != null) && (parameters.contains(RequestKey.HW_AI_MOVIE))) {
            List<Byte> lists = mModeCharacteristics.getParameterRange(RequestKey.HW_AI_MOVIE);
            ranges = new Byte[lists.size()];
            lists.toArray(ranges);
        }
        initSpinner(R.id.aiMovieSpinner, byteToList(ranges, R.string.aiMovie), new SpinnerOperation() {
            @Override
            public void doOperation(String text) {
                try {
                    mMode.setParameter(RequestKey.HW_AI_MOVIE, Byte.valueOf(text.split(PIVOT)[1]));
                } catch (PatternSyntaxException e) {
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
                mBackgroundHandler.post(() -> {
                    operation.doOperation(text);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            Log.v(TAG, "Releasing media recorder.");
            try {
                mMediaRecorder.reset();
            } catch (IllegalStateException e) {
                Log.e(TAG, "media recorder maybe has been released! msg=" + e.getMessage());
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * start recording
     */
    private void startRecord() {
        try {
            acquiremStartStopRecordLock();
            mRecordState = CameraKitHelper.RecordState.PRE_PROCESS;
            if (!mIsFirstRecord) {
                setUpMediaRecorder(mRecordSize, mVideoSurface);
            }
            mIsFirstRecord = false;
            mMode.startRecording();
            mMediaRecorder.start();
            runOnUiThread(() -> {
                mButtonRecordPause.setVisibility(View.VISIBLE);
                mButtonVideo.setText(R.string.stoprecord);
                mButtonVideo.setEnabled(true);
            });
            mRecordState = CameraKitHelper.RecordState.RECORDING;
            Log.d(TAG, "Recording starts!");
        } catch (InterruptedException e) {
            Log.e(TAG, "acquiremStartStopRecordLock failed");
        } catch (IllegalStateException e) {
            Log.e(TAG, "mMediaRecorder prepare not well!");
            clearInvalidFile();
            mRecordState = CameraKitHelper.RecordState.IDLE;
        } finally {
            releasemStartStopRecordLock();
        }
    }

    private void stopRecord() {
        try {
            acquiremStartStopRecordLock();
            mMode.stopRecording();
            mMediaRecorder.stop();
        } catch (InterruptedException e) {
            Log.e(TAG, "acquiremStartStopRecordLock failed");
        } catch (IllegalStateException e) {
            Log.e(TAG, "mMediaRecorder stop state error");
        } catch (RuntimeException stopException) {
            Log.e(TAG, "going to clean up the invalid output file");
            clearInvalidFile();
        } finally {
            mRecordState = CameraKitHelper.RecordState.IDLE;
            runOnUiThread(() -> {
                mButtonRecordPause.setVisibility(View.INVISIBLE);
                mButtonVideo.setText(R.string.record);
            });
            releasemStartStopRecordLock();
        }
    }

    private void pauseRecord() {
        if (mRecordState == CameraKitHelper.RecordState.RECORDING) {
            Toast.makeText(this, "pauseRecord", Toast.LENGTH_LONG).show();
            try {
                acquiremStartStopRecordLock();
                mMode.pauseRecording();
                mMediaRecorder.pause();
                mRecordState = CameraKitHelper.RecordState.PAUSED;
                runOnUiThread(() -> {
                    mButtonRecordPause.setText(R.string.resume);
                    mButtonVideo.setEnabled(false);
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted while trying to acquire start stop lock when pauseRecord" + e.getCause());
            } catch (IllegalStateException e) {
                Log.e(TAG, "mMediaRecorder pause state error");
            } finally {
                releasemStartStopRecordLock();
            }
        }
    }

    private void resumeRecord() {
        Toast.makeText(this, "resumeRecord", Toast.LENGTH_LONG).show();
        if (mRecordState == CameraKitHelper.RecordState.PAUSED) {
            Log.d(TAG, "[schedule] resume recording");
            try {
                acquiremStartStopRecordLock();
                mMode.resumeRecording();
                mMediaRecorder.resume();
                mRecordState = CameraKitHelper.RecordState.RECORDING;
                runOnUiThread(() -> {
                    mButtonRecordPause.setText(R.string.pause);
                    mButtonVideo.setEnabled(true);
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted while trying to acquire start stop lock when resumeRecord " + e.getCause());
            } catch (IllegalStateException e) {
                Log.e(TAG, "mMediaRecorder resume state error");
            } finally {
                releasemStartStopRecordLock();
            }
        }
    }

    private void acquiremStartStopRecordLock() throws InterruptedException {
        if (mStartStopRecordLock != null) {
            mStartStopRecordLock.acquire();
        } else {
            Log.d(TAG, "acquiremStartStopRecordLock, mStartStopRecordLock refer null");
        }
    }

    private void releasemStartStopRecordLock() {
        if (mStartStopRecordLock != null) {
            if (mStartStopRecordLock.availablePermits() < 1) {
                mStartStopRecordLock.release();
            }
        } else {
            Log.d(TAG, "release lock, but it is null");
        }
    }

    private void clearInvalidFile() {
        if (!mVideoFile.isEmpty()) {
            File vidFile = new File(mVideoFile);
            if (vidFile.exists()) {
                vidFile.delete();
                mVideoFile = "";
                Log.d(TAG, "invalid video file deleted!");
            }
        }
    }

    private List<String> byteToList(Byte[] values, int id) {
        List<String> lists = new ArrayList<>(0);
        if ((values == null) || (values.length == 0)) {
            Log.d(TAG, "getIntList, values is null");
            return lists;
        }
        for (byte mode : values) {
            lists.add(getString(id) + PIVOT + mode);
        }
        return lists;
    }
}