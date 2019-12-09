package com.didi.aoe.library.logging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.logging.impl.AoeLoggerImpl;

/**
 * @author noctis
 */
public final class LoggerFactory {
    private static LoggerBinder sLoggerBinder;

    private LoggerFactory() {
    }

    @NonNull
    public static Logger getLogger(String tag) {
        Logger logger = null;
        if (sLoggerBinder != null) {
            logger = sLoggerBinder.getLogger(tag);
        }
        if (logger != null) {
            return logger;
        }
        return generalDefaultLogger(tag);
    }

    @NonNull
    private static Logger generalDefaultLogger(String tag) {
        sLoggerBinder = new LoggerBinder() {
            @Override
            public Logger getLogger(String tag) {
                return new AoeLoggerImpl(tag);
            }
        };
        return sLoggerBinder.getLogger(tag);
    }

    public static void setLoggerBinder(@Nullable LoggerBinder binder) {
        sLoggerBinder = binder;
    }

}
