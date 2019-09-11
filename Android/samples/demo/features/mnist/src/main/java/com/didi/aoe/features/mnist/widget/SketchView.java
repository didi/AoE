package com.didi.aoe.features.mnist.widget;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.didi.aoe.features.mnist.render.SketchRenderer;

/**
 * @author noctis
 */
public class SketchView extends View implements LifecycleObserver {
    private Lifecycle mLifecycle;
    private SketchRenderer mRenderer;

    public SketchView(Context context) {
        super(context);
    }

    public SketchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SketchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SketchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setRenderer(SketchRenderer renderer) {
        this.mRenderer = renderer;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRenderer != null) {
            mRenderer.render(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRenderer != null) {
            boolean result = mRenderer.handleTouchEvent(event);
            if (result) {
                invalidate();
            }
            return result;
        }

        return super.onTouchEvent(event);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (mRenderer != null) {
            mRenderer.createBitmap();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        if (mRenderer != null) {
            mRenderer.releaseBitmap();
        }

    }

    public void setLifecycleOwner(@NonNull LifecycleOwner owner) {
        if (mLifecycle != null) mLifecycle.removeObserver(this);
        mLifecycle = owner.getLifecycle();
        mLifecycle.addObserver(this);
    }

    public void resetAndInvalidate() {
        if (mRenderer != null) {
            mRenderer.reset();
        }
        invalidate();
    }
}
