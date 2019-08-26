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
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Crop(JNIEnv *env, jclass clazz,
                                                       jbyteArray nv21_src, jint src_width,
                                                       jint src_height, jint crop_x, jint crop_y,
                                                       jint crop_width, jint crop_height) {
    // TODO: implement NV21Crop()
}

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Rotate(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint degree) {
    // TODO: implement NV21Rotate()
}

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Resize(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint out_width,
                                                         jint out_height) {
    // TODO: implement NV21Resize()
}

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Convert(JNIEnv *env, jclass clazz,
                                                          jbyteArray nv21_src, jint src_width,
                                                          jint src_height, jint degree, jint crop_x,
                                                          jint crop_y, jint out_width,
                                                          jint out_height) {
    // TODO: implement NV21Convert()
}

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21ToRGBA(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint degree, jint crop_x,
                                                         jint crop_y, jint out_width,
                                                         jint out_height) {
    // TODO: implement NV21ToRGBA()
}


#ifdef __cplusplus
}
#endif
