//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_TENSOR_JNI_H
#define AOE_ANDROID_TENSOR_JNI_H

#include <jni.h>

#define NCNNJNI_METHOD2(METHOD_NAME) \
  Java_com_didi_aoe_runtime_tengine_Tensor_##METHOD_NAME

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD2(create)(JNIEnv *env, jclass clazz, jlong interpreterHandle, jint tensorIndex,
                        jboolean isInput);

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(delete)(JNIEnv *env, jclass clazz, jlong handle);

JNIEXPORT jint JNICALL
NCNNJNI_METHOD2(inputRgbaResize)(JNIEnv *env, jclass clazz, jbyteArray rgbaDate,
                                jint srcWidth, jint srcHeight, jint dstWidth, jint dstHeight,
                                jfloatArray channelMeanVals, jboolean toBrg, jfloat scale, jlong handler);

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(writeDirectBuffer)(JNIEnv *env, jclass clazz, jlong handle, jobject src);

JNIEXPORT jobject JNICALL
NCNNJNI_METHOD2(buffer)(JNIEnv *env, jclass clazz, jlong handler);

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(resizeInputShape)(JNIEnv *env, jclass clazz, jintArray dims, jlong handle);

JNIEXPORT jintArray JNICALL
NCNNJNI_METHOD2(shape)(JNIEnv *env, jclass clazz, jlong handle);

JNIEXPORT jint JNICALL
NCNNJNI_METHOD2(numBytes)(JNIEnv *env, jclass clazz, jlong handle);

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(readMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler, jobject value);

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(writeMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler, jobject src);

#ifdef __cplusplus
}
#endif

#endif //AOE_ANDROID_TENSOR_JNI_H
