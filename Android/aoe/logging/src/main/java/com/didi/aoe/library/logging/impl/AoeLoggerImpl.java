package com.didi.aoe.library.logging.impl;

import android.util.Log;

import com.didi.aoe.library.logging.Logger;

/**
 * @author noctis
 */
public class AoeLoggerImpl implements Logger {
    private String mTag = "AoeLogger";

    public AoeLoggerImpl(String tag) {
        mTag = tag;
    }

    @Override
    public void debug(String msg, Object... args) {
        if (args != null && args.length > 0) {
            Log.d(mTag, String.format(msg, args));
        } else {
            Log.d(mTag, msg);
        }
    }

    @Override
    public void info(String msg, Object... args) {
        if (args != null && args.length > 0) {
            Log.i(mTag, String.format(msg, args));
        } else {
            Log.i(mTag, msg);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        Log.w(mTag, msg, t);
    }

    @Override
    public void warn(String msg, Object... args) {
        if (args != null && args.length > 0) {
            Log.w(mTag, String.format(msg, args));
        } else {
            Log.w(mTag, msg);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        Log.e(mTag, msg, t);
    }

    @Override
    public void error(String msg, Object... args) {
        if (args != null && args.length > 0) {
            Log.e(mTag, String.format(msg, args));
        } else {
            Log.e(mTag, msg);
        }
    }
}
