
package com.example.hmsdemo.cameraKit.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * The textture view used for preview
 */
public class AutoFitTextureView extends TextureView {
    private int mRatioWidth = 0;

    private int mRatioHeight = 0;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set textture view ratio
     *
     * @param width the textture width
     * @param height the textture height
     */
    public void setAspectRatio(int width, int height) {
        if ((width < 0) || (height < 0)) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if ((0 == mRatioWidth) || (0 == mRatioHeight)) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
