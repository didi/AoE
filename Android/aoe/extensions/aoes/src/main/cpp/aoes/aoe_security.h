#ifndef PROJECT_AOE_SECURITY_H
#define PROJECT_AOE_SECURITY_H

#include <jni.h>

#define TAG "AoES"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_aoes_AoeSecurity_encrypt(JNIEnv *env, jclass type, jbyteArray src, jint srcLength);

JNIEXPORT jint JNICALL
Java_com_didi_aoe_extensions_aoes_AoeSecurity_encryptToFile(JNIEnv *env, jclass type, jbyteArray src, jint srcLength, jstring destFilePath);


#ifdef __cplusplus
}
#endif


#endif //PROJECT_AOE_SECURITY_H