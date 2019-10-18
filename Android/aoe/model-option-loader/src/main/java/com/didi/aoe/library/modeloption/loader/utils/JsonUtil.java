/*
 * Copyright 2019 The AoE Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            mLogger.error("jsonFromObject: ", e);
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
            mLogger.error("objectFromJson: ", e);
        }
        return null;
    }
}
