package com.didi.aoe.examples.demo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author noctis
 */
class ShowcaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        //        Aoe.init(getContext(), new AoeDataProvider() {
        //            @Override
        //            public long appId() {
        //                return 164;
        //            }
        //
        //            @Override
        //            public double latitude() {
        //                return 39.92;
        //            }
        //
        //            @Override
        //            public double longitude() {
        //                return 116.46;
        //            }
        //        });
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }
}