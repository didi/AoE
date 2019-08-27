//
// Created by noctis on 2019/3/15.
//
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <libyuv.h>
#include "aoe_vision.h"

#define posit(x) std::max(std::min(x, 1.0f), 0.0f)

using namespace std;

using namespace libyuv;

// JNI -------------------------

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21ToARGB(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src,
                                                         jint src_width, jint src_height,
                                                         jint degree,
                                                         jint crop_x, jint crop_y,
                                                         jint crop_width, jint crop_height,
                                                         jint out_width, jint out_height) {
    if (crop_x < 0) {
        crop_x = 0;
    }

    if (crop_y < 0) {
        crop_y = 0;
    }

    crop_height = crop_height >> 1 << 1;
    out_height = out_height >> 1 << 1;

    int cropWidth = crop_width;
    int cropHeight = crop_height;
    if (crop_x + cropWidth > src_width) {
        cropWidth = src_width - crop_x;
    }

    if (crop_y + cropHeight > src_height) {
        cropHeight = src_height - crop_y;
    }

    jbyte *nv21SrcData = env->GetByteArrayElements(nv21_src, NULL);
    int responseDimens = (cropWidth * cropHeight * 3) >> 1;

    int dstWidth = cropWidth;
    int dstHeight = cropHeight;
    if (degree == 90 || degree == 270) {
        dstWidth = cropHeight;
        dstHeight = cropWidth;
    }

    // 角度
    const enum RotationMode rotateMode = (enum RotationMode) degree;

    // 裁剪和旋转
    uint8 *clipResult = new uint8[responseDimens];
    libyuv::ConvertToI420((const uint8 *) nv21SrcData, (src_width * src_height * 3) >> 1,
                          clipResult,
                          dstWidth,
                          clipResult + dstWidth * dstHeight, dstWidth >> 1,
                          clipResult + ((dstWidth * dstHeight * 5) >> 2), dstWidth >> 1, crop_x,
                          crop_y, src_width, src_height,
                          cropWidth, cropHeight,
                          rotateMode,
                          libyuv::FOURCC_NV21);

    responseDimens = out_width * out_height * 4;
    uint8 *rgba = new uint8[responseDimens];

    if (dstWidth == out_width && dstHeight == out_height) {

        libyuv::I420ToRGBA(clipResult, out_width, clipResult + out_width * out_height, out_width >> 1,
                           clipResult + ((out_width * out_height * 5) >> 2), out_width >> 1, rgba,
                           out_width * 4, out_width, out_height);
    } else {
        // 需要缩放
        uint8 *scaleResult = new uint8[out_width * out_height * 3 / 2];
        libyuv::I420Scale(clipResult, dstWidth,
                          clipResult + dstWidth * dstHeight, dstWidth / 2,
                          clipResult + dstWidth * dstHeight * 5 / 4, dstWidth / 2,
                          dstWidth, dstHeight,
                          scaleResult, out_width,
                          scaleResult + out_width * out_height, out_width / 2,
                          scaleResult + out_width * out_height * 5 / 4, out_width / 2,
                          out_width, out_height, FilterMode::kFilterBilinear);

        libyuv::I420ToRGBA(scaleResult, out_width, scaleResult + out_width * out_height, out_width >> 1,
                           scaleResult + ((out_width * out_height * 5) >> 2), out_width >> 1, rgba,
                           out_width * 4, out_width, out_height);

        delete[] scaleResult;
    }

    jbyteArray response = env->NewByteArray(responseDimens);
    env->SetByteArrayRegion(response, 0, responseDimens, (jbyte *) rgba);
    env->ReleaseByteArrayElements(nv21_src, nv21SrcData, 0);

    delete[] clipResult;
    delete[] rgba;
    return response;
}

#ifdef __cplusplus
}
#endif
