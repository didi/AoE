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
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author noctis
 * @since 1.0.3
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class FileUtils {
    private static final Logger mLogger = LoggerFactory.getLogger("FileUtils");

    /**
     * 获取文件根目录，有外置存储时优先用外置空间
     *
     * @param context 应用上下文
     * @return
     */
    public static String getFilesDir(@NonNull Context context) {
        if (isExternalMediaAvailable()) {
            File filesDir = Objects.requireNonNull(context.getExternalFilesDir(null));
            return filesDir.getAbsolutePath();
        }
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * 有可用外置存储
     *
     * @return
     */
    public static boolean isExternalMediaAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable();
    }

    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    @Nullable
    public static String readString(String filePath) {
        byte[] result = read(filePath);
        if (result == null) {
            return null;
        } else {
            return new String(result);
        }
    }

    @Nullable
    public static String readString(InputStream is) {
        byte[] result = read(is);
        if (result == null) {
            return null;
        } else {
            return new String(result);
        }
    }

    /**
     * 读取文件路径字节流数组
     *
     * @param filePath 文件全路径
     * @return
     */
    @Nullable
    public static byte[] read(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return read(fis);
        } catch (Exception e) {
            mLogger.error("read file exception: ", e);
        }
        return null;
    }

    /**
     * 读取字节流数组
     *
     * @param is 输入流
     * @return
     */
    @Nullable
    public static byte[] read(InputStream is) {

        try (BufferedInputStream bis = new BufferedInputStream(is);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

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
