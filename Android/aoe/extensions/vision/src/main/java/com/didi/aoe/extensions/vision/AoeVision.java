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

package com.didi.aoe.extensions.vision;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

/**
 * @author noctis
 */
public class AoeVision {
    private static final Logger mLogger = LoggerFactory.getLogger("AoeVision");

    /**
     * NV21 格式图像转为 ARGB 格式
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @return
     */
    public static byte[] NV21ToARGB(byte[] nv21Src, int srcWidth, int srcHeight) {
        return NV21ToARGB(nv21Src, srcWidth, srcHeight, srcWidth, srcHeight);
    }

    /**
     * NV21 格式图像缩放为 ARGB 格式图像
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static byte[] NV21ToARGB(byte[] nv21Src, int srcWidth, int srcHeight, int outWidth, int outHeight) {
        return NV21ToARGB(nv21Src, srcWidth, srcHeight, 0, 0, 0, srcWidth, srcHeight, outWidth, outHeight);
    }


    /**
     * NV21 格式图像的裁剪、翻转、缩放
     *
     * @param nv21Src
     * @param srcWidth
     * @param srcHeight
     * @param rotation   can be 0, 90, 180 or 270.
     * @param cropX
     * @param cropY
     * @param cropWidth
     * @param cropHeight
     * @param outWidth
     * @param outHeight
     * @return 0 for successful; -1 for invalid parameter. Non-zero for failure.
     */
    public static native byte[] NV21ToARGB(byte[] nv21Src, int srcWidth, int srcHeight, int rotation, int cropX, int cropY, int cropWidth, int cropHeight, int outWidth, int outHeight);

    static {
        try {
            System.loadLibrary("aoe_vision");
            mLogger.debug("aoe_vision init success");
        } catch (UnsatisfiedLinkError e) {
            mLogger.info("library not found!");
        }
    }
}
