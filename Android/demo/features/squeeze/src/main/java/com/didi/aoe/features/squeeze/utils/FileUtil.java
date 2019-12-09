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

package com.didi.aoe.features.squeeze.utils;


import androidx.annotation.RestrictTo;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件处理工具类
 *
 * @author noctis
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class FileUtil {
    private static final Logger mLogger = LoggerFactory.getLogger("FileUtil");

    private FileUtil() {
    }

    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static byte[] read(InputStream is) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            bis = new BufferedInputStream(is);
            baos = new ByteArrayOutputStream();

            int len;

            byte[] buf = new byte[1024];

            while ((len = bis.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            return baos.toByteArray();

        } catch (Exception e) {
            mLogger.error(e.toString());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private static byte[] read(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            return read(fis);
        } catch (Exception e) {
            mLogger.error(e.toString());
        }
        return null;
    }

    public static String readString(String filePath) {
        byte[] bytes = read(filePath);
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    public static String readString(InputStream is) {
        byte[] bytes = read(is);
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

}
