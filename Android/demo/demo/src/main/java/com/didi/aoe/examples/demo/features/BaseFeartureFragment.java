package com.didi.aoe.examples.demo.features;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.didi.aoe.examples.demo.BaseFragment;

/**
 * @author noctis
 */
public class BaseFeartureFragment extends BaseFragment {
    @SuppressWarnings("WeakerAccess")
    protected final String TAG = getClass().getSimpleName();
    private final Handler mHandler = new Handler();
    private Thread mUiThread;
    private HandlerThread mWorkerHandler = null;
    private Handler mWorker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[onCreate]");
        mUiThread = Thread.currentThread();
        mWorkerHandler = new HandlerThread("worker-" + TAG);
        mWorkerHandler.start();
        mWorker = new Handler(mWorkerHandler.getLooper());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "[onViewCreated]");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "[onResume]");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "[onPause]");
        mHandler.removeCallbacksAndMessages(null);
        if (mWorker != null) {
            mWorker.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "[onDestroyView]");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[onDestroy]");
        mHandler.removeCallbacksAndMessages(null);
        if (mWorkerHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mWorkerHandler.quitSafely();
            } else {
                mWorkerHandler.quit();
            }
        }
        if (mWorker != null) {
            mWorker.removeCallbacksAndMessages(null);
        }

    }

    protected final void run(@NonNull Runnable runnable) {
        mWorker.post(runnable);
    }

    protected final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
}
