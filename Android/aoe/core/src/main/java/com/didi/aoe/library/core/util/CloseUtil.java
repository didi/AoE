package com.didi.aoe.library.core.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * 资源释放工具
 *
 * @author noctis
 */
public final class CloseUtil {
    private CloseUtil() {
    }

    /**
     * 关闭多个IO流
     *
     * @param closeables io,io,io,...
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
