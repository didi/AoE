package com.didi.aoe.extensions.vision;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

/**
 * @author noctis
 */
public class AoeVision {
    private static final Logger mLogger = LoggerFactory.getLogger("AoeVision");

    /**
     * NV21 图像区域截取
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param cropX
     * @param cropY
     * @param cropWidth
     * @param cropHeight
     * @return
     */
    public static native byte[] NV21Crop(byte[] nv21Src, int srcWidth, int srcHeight, int cropX, int cropY, int cropWidth, int cropHeight);

    /**
     * NV21 图像旋转
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param degree
     * @return
     */
    public static native byte[] NV21Rotate(byte[] nv21Src, int srcWidth, int srcHeight, int degree);

    /**
     * NV21 图像缩放
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static native byte[] NV21Resize(byte[] nv21Src, int srcWidth, int srcHeight, int outWidth, int outHeight);

    /**
     * NV21 图像转换，复合 截取、旋转和缩放
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param degree
     * @param cropX
     * @param cropY
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static native byte[] NV21Convert(byte[] nv21Src, int srcWidth, int srcHeight, int degree, int cropX, int cropY, int outWidth, int outHeight);

    /**
     * NV21 格式转换为 RGBA
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param degree
     * @param cropX
     * @param cropY
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static native byte[] NV21ToRGBA(byte[] nv21Src, int srcWidth, int srcHeight, int degree, int cropX, int cropY, int outWidth, int outHeight);

    static {
        try {
            System.loadLibrary("aoe_vision");
            mLogger.debug("aoe_vision init success");
        } catch (UnsatisfiedLinkError e) {
            mLogger.info("library not found!");
        }
    }
}
