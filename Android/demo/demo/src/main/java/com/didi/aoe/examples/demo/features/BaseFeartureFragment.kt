package com.didi.aoe.examples.demo.features

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View

import com.didi.aoe.examples.demo.BaseFragment

/**
 * @author noctis
 */
open class BaseFeartureFragment : BaseFragment() {
    protected val TAG = javaClass.simpleName
    private val mHandler = Handler()
    private var mUiThread: Thread? = null
    private lateinit var mWorkerHandler: HandlerThread
    private lateinit var mWorker: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "[onCreate]")
        mUiThread = Thread.currentThread()
        mWorkerHandler = HandlerThread("worker-$TAG")
        mWorkerHandler.start()
        mWorker = Handler(mWorkerHandler.looper)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "[onViewCreated]")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "[onResume]")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "[onPause]")
        mHandler.removeCallbacksAndMessages(null)
        mWorker.removeCallbacksAndMessages(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "[onDestroyView]")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "[onDestroy]")
        mHandler.removeCallbacksAndMessages(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerHandler.quitSafely()
        } else {
            mWorkerHandler.quit()
        }
        mWorker.removeCallbacksAndMessages(null)

    }

    protected fun run(runnable: Runnable) {
        mWorker.post(runnable)
    }

    protected fun runOnUiThread(action: Runnable) {
        if (Thread.currentThread() !== mUiThread) {
            mHandler.post(action)
        } else {
            action.run()
        }
    }
}
