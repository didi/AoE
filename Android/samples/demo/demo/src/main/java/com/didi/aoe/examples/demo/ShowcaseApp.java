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
    }
}