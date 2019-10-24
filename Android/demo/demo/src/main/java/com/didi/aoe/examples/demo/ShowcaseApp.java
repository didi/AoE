package com.didi.aoe.examples.demo;

import android.app.Application;
import android.content.Context;

/**
 * @author noctis
 */
public class ShowcaseApp extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

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
}