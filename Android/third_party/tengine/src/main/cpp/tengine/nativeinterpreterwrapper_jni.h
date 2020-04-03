//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_TENGINE_H
#define AOE_ANDROID_TENGINE_H

#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define TENGINEJNI_METHOD(METHOD_NAME) \
  Java_com_didi_aoe_runtime_tengine_NativeInterpreterWrapper_##METHOD_NAME  // NOLINT

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
TENGINEJNI_METHOD(createInterpreter)(JNIEnv *env, jclass clazz);

JNIEXPORT jstring JNICALL
TENGINEJNI_METHOD(getTengineVersion)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT void JNICALL
TENGINEJNI_METHOD(delete)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
TENGINEJNI_METHOD(loadTengineModelFromPath)(JNIEnv *env, jclass clazz, jstring aModelPath,
                                  jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
TENGINEJNI_METHOD(loadModelFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
                                    jstring folderName, jstring fileName, jlong interpreterHandle);

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(getInputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(getOutputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT jlong JNICALL
TENGINEJNI_METHOD(allocateTensors)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT void JNICALL
TENGINEJNI_METHOD(inputRgbaToBgr)(JNIEnv *env, jclass clazz, jbyteArray rgbaDate,
                          jint srcWidth, jint srcHeight, jint dstWidth, jint dstHeight,
                          jfloatArray channelMeanVals, jlong tensorHandle);

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(run)(JNIEnv *env, jclass clazz, jlong interpreterHandle);


#ifdef __cplusplus
}
#endif

#endif //AOE_ANDROID_TENGINE_H
