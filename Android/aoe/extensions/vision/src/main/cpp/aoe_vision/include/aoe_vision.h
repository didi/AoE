//
// Created by noctis on 18/7/26.
//

#ifndef AOE_VISION_H
#define AOE_VISION_H

#include <string>
#include <jni.h>

#define TAG "AoeVision"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Crop(JNIEnv *env, jclass clazz,
                                                       jbyteArray nv21_src, jint src_width,
                                                       jint src_height, jint crop_x, jint crop_y,
                                                       jint crop_width, jint crop_height);

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Rotate(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint degree);

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Resize(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint out_width,
                                                         jint out_height);

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21Convert(JNIEnv *env, jclass clazz,
                                                          jbyteArray nv21_src, jint src_width,
                                                          jint src_height, jint degree, jint crop_x,
                                                          jint crop_y, jint out_width,
                                                          jint out_height);

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_vision_AoeVision_NV21ToRGBA(JNIEnv *env, jclass clazz,
                                                         jbyteArray nv21_src, jint src_width,
                                                         jint src_height, jint degree, jint crop_x,
                                                         jint crop_y, jint out_width,
                                                         jint out_height);

#ifdef __cplusplus
}
#endif


#endif //AOE_VISION_H
