package com.didi.aoe.features.mnist.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.view.MotionEvent;

import com.didi.aoe.features.mnist.model.SketchModel;

/**
 * @author noctis
 */
public class SketchRenderer {
    @NonNull
    private final SketchModel mModel;

    private final Paint mPaint = new Paint();

    // 28x28 pixel Bitmap
    private final Matrix mMatrix = new Matrix();
    private final Matrix mInvMatrix = new Matrix();
    private final float[] mTmpPoints = new float[2];
    private final PointF mTmpPoint = new PointF();
    private Canvas mSketchCanvas;
    private float mLastX;
    private float mLastY;
    private int mDrawnLineSize = 0;
    private boolean initialized = false;

    public SketchRenderer(@NonNull SketchModel model) {
        this.mModel = model;
        mPaint.setAntiAlias(true);
    }

    public void render(@NonNull Canvas canvas) {
        if (mSketchCanvas == null) {
            return;
        }
        if (!initialized) {
            setup(canvas.getWidth(), canvas.getHeight());
            initialized = true;
        }

        int startIndex = mDrawnLineSize - 1;
        if (startIndex < 0) {
            startIndex = 0;
        }

        int lineSize = mModel.getLineSize();
        mPaint.setColor(Color.BLACK);
        for (int i = startIndex; i < lineSize; ++i) {
            SketchModel.Line line = mModel.getLine(i);

            int sizes = line.sizes();
            if (sizes < 1) {
                continue;
            }
            PointF p = line.get(0);
            float lastX = p.x;
            float lastY = p.y;

            for (int j = 0; j < sizes; ++j) {
                p = line.get(j);
                float x = p.x;
                float y = p.y;
                mSketchCanvas.drawLine(lastX, lastY, x, y, mPaint);
                lastX = x;
                lastY = y;
            }
        }
        mDrawnLineSize = mModel.getLineSize();
        Bitmap bitmap = mModel.getBitmap();
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, mMatrix, mPaint);
        }
    }

    public void createBitmap() {
        mSketchCanvas = new Canvas(mModel.createBitmap());
        reset();
    }


    public void releaseBitmap() {
        mModel.releaseBitmap();
        mSketchCanvas = null;
        reset();
    }

    public void reset() {
        mDrawnLineSize = 0;
        mPaint.setColor(Color.WHITE);
        if (mSketchCanvas != null) {
            mSketchCanvas.drawRect(new Rect(0, 0, mModel.getWidth(), mModel.getHeight()), mPaint);
        }
    }

    private void setup(float width, float height) {
        // Model (bitmap) size
        float modelWidth = mModel.getWidth();
        float modelHeight = mModel.getHeight();

        float scaleW = width / modelWidth;
        float scaleH = height / modelHeight;

        float scale = scaleW;
        if (scale > scaleH) {
            scale = scaleH;
        }

        float newCx = modelWidth * scale / 2;
        float newCy = modelHeight * scale / 2;
        float dx = width / 2 - newCx;
        float dy = height / 2 - newCy;

        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(dx, dy);
        mMatrix.invert(mInvMatrix);
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.paintTo(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.paintTo(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
    }

    private void processTouchUp() {
        mModel.pause();
    }

    private void calcPos(float x, float y, PointF out) {
        mTmpPoints[0] = x;
        mTmpPoints[1] = y;
        mInvMatrix.mapPoints(mTmpPoints);
        out.x = mTmpPoints[0];
        out.y = mTmpPoints[1];
    }

    public boolean handleTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }

        return false;
    }
}
