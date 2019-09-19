package com.didi.aoe.library.modeloption.loader.utils;

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
public class FileUtil {
    private static final Logger mLogger = LoggerFactory.getLogger("FileUtil");

    private FileUtil() {
    }

    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private static byte[] read(InputStream is) {
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
