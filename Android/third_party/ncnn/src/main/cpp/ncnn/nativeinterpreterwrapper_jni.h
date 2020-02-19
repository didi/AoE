//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_NCNN_H
#define AOE_ANDROID_NCNN_H

#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define NCNNJNI_METHOD(METHOD_NAME) \
  Java_com_didi_aoe_runtime_ncnn_NativeInterpreterWrapper_##METHOD_NAME  // NOLINT

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(createInterpreter)(JNIEnv *env, jclass clazz, jboolean lightMode, jint numThreads);

JNIEXPORT void JNICALL
NCNNJNI_METHOD(delete)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadModel)(JNIEnv *env, jclass clazz, jstring filePath, jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadParam)(JNIEnv *env, jclass clazz, jstring filePath, jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadModelFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
                                    jstring folderName, jstring fileName, jlong interpreterHandle);

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadParamFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
                                    jstring folderName, jstring fileName, jlong interpreterHandle);

JNIEXPORT void JNICALL
NCNNJNI_METHOD(setBlobIndex)(JNIEnv *env, jclass clazz, jint inputBlobIndex, jint outputBlobIndex,
                             jlong interpreterHandle);

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getInputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle);


JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getOutputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle);

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(allocateTensors)(JNIEnv *env, jclass clazz, jlong interpreterHandle, jint inputCount,
                                jint outputCount);

JNIEXPORT void JNICALL
NCNNJNI_METHOD(inputRgba)(JNIEnv *env, jclass clazz, jbyteArray rgbaDate,
                          jint srcWidth, jint srcHeight, jint dstWidth, jint dstHeight,
                          jfloatArray meanVals, jfloatArray normVals, jlong interpreterHandle,
                          jlong tensorHandle);

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(run)(JNIEnv *env, jclass clazz, jlong interpreterHandle);


#ifdef __cplusplus
}
#endif

#endif //AOE_ANDROID_NCNN_H
