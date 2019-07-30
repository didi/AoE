package com.didi.aoe.library.modeloption.loader.utils;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.google.gson.Gson;

/**
 * Be aware that,normal inner class is not support to convert from json string.
 */
public class JsonUtil {
    private static final Logger mLogger = LoggerFactory.getLogger("JsonUtil");

    public static String jsonFromObject(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return new Gson().toJson(object);
        } catch (Exception e) {
            mLogger.error("JsonUtil", e.getMessage());
        }
        return null;
    }

    public static <T> T objectFromJson(String json, Class<T> klass) {
        if (json == null) {
            return null;
        }
        try {
            return new Gson().fromJson(json, klass);
        } catch (Exception e) {
            mLogger.error("JsonUtil", e.getMessage());
        }
        return null;
    }
}
