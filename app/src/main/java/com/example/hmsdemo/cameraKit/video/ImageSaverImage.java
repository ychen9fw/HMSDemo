package com.example.hmsdemo.cameraKit.video;

import android.media.Image;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * save image
 */
public class ImageSaverImage implements Runnable {
    private static final String TAG = ImageSaverImage.class.getSimpleName();

    private final Image mImage;

    private final File mFile;

    public ImageSaverImage(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
            output.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "IOException when write in run");
        } finally {
            mImage.close();
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException when close in run");
                }
            }
        }
    }
}