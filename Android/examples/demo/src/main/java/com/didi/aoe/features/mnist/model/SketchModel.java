package com.didi.aoe.features.mnist.model;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noctis
 */
public class SketchModel implements Serializable {
    private final int mWidth;  // pixel width = 28
    private final int mHeight; // pixel height = 28
    private final List<Line> mLines = new ArrayList<>();

    private Line mCurrentLine;

    private Bitmap mSketchBitmap;

    public SketchModel() {
        this(0, 0);
    }

    public SketchModel(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void paintTo(float x, float y) {
        if (mCurrentLine == null) {
            mCurrentLine = new Line();
            mCurrentLine.addPoint(x, y);
            mLines.add(mCurrentLine);
        } else {
            mCurrentLine.addPoint(x, y);
        }
    }

    public void pause() {
        mCurrentLine = null;
    }

    public void clear() {
        mLines.clear();
    }

    public int getLineSize() {
        return mLines.size();
    }

    /**
     * Get 28x28 pixel data for tensorflow input.
     */
    public float[] getPixelData() {
        if (mSketchBitmap == null) {
            return null;
        }

        int width = mSketchBitmap.getWidth();
        int height = mSketchBitmap.getHeight();

        // Get 28x28 pixel data from bitmap
        int[] pixels = new int[width * height];
        mSketchBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] retPixels = new float[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            // Set 0 for white and 255 for black pixel
            int pix = pixels[i];
            int b = pix & 0xff;
            retPixels[i] = (float) (0xff - b);
        }
        return retPixels;
    }

    public int[] getPixelIntData() {
        if (mSketchBitmap == null) {
            return null;
        }

        int width = mSketchBitmap.getWidth();
        int height = mSketchBitmap.getHeight();

        // Get 28x28 pixel data from bitmap
        int[] pixels = new int[width * height];
        mSketchBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        return pixels;
    }

    public Bitmap createBitmap() {
        if (mSketchBitmap != null) {
            mSketchBitmap.recycle();
        }
        mSketchBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        return mSketchBitmap;
    }

    public void releaseBitmap() {
        if (mSketchBitmap != null) {
            mSketchBitmap.recycle();
            mSketchBitmap = null;
        }
    }

    public Bitmap getBitmap() {
        Log.d("SketchModel","===mSketchBitmap:"+mSketchBitmap);
        return mSketchBitmap;
    }

    public Line getLine(int index) {
        return mLines.get(index);
    }

    public static class Line {
        private final List<PointF> points = new ArrayList<>();

        private Line() {
        }

        private void addPoint(PointF point) {
            points.add(point);
        }

        private void addPoint(float x, float y) {
            addPoint(new PointF(x, y));
        }

        public int sizes() {
            return points.size();
        }

        public PointF get(int index) {
            return points.get(index);
        }
    }
}
