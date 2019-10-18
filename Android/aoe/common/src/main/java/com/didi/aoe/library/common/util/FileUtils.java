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

package com.didi.aoe.library.common.util;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author noctis
 * @since 1.1.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class FileUtils {
    private static final Logger mLogger = LoggerFactory.getLogger("FileUtils");

    public static String getFilesDir(@NonNull Context context) {
        if (isExternalMediaAvailable()) {
            return context.getExternalFilesDir(null).getAbsolutePath();
        }
        return context.getFilesDir().getAbsolutePath();
    }

    private static boolean isExternalMediaAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            // No SD card found.
            return true;
        }
        return false;
    }

    public static byte[] read(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            return read(fis);
        } catch (Exception e) {
            mLogger.error("read file exception: ", e);
        }
        return null;
    }

    public static byte[] read(InputStream is) {

        try (BufferedInputStream bis = new BufferedInputStream(is); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            int len;

            byte[] buf = new byte[1024];

            while ((len = bis.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            return baos.toByteArray();

        } catch (Exception e) {
            mLogger.error("read IO exception: ", e);
        }
        return null;
    }
}
