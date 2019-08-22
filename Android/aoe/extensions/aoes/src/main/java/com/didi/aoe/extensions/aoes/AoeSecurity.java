package com.didi.aoe.extensions.aoes;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

/**
 * @author noctis
 */
public class AoeSecurity {
    private static final Logger mLogger = LoggerFactory.getLogger("AoeSecurity");

    /**
     * 采用AEO Version1加密
     *
     * @param src       原始数据
     * @param srcLength 原始数据长度
     * @return 加密后的数据，加密失败返回NULL
     */
    public static native byte[] encrypt(byte[] src, int srcLength);

    /**
     * 采用AoE Version1加密
     *
     * @param src          原始数据
     * @param srcLength    原始数据长度
     * @param destFilePath 加密后的文件
     * @return 加密后的数据长度，加密失败返回-1
     */
    public static native int encryptToFile(byte[] src, int srcLength, String destFilePath);

    static {
        try {
            System.loadLibrary("aoes");
            mLogger.debug("aoes init success");
        } catch (UnsatisfiedLinkError e) {
            mLogger.info("library not found!");
        }
    }
}
