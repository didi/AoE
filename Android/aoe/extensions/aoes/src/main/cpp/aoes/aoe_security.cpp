#include <aoe_security.h>
#include "aoes.h"


#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_didi_aoe_extensions_aoes_AoeSecurity_encrypt(JNIEnv *env, jclass type, jbyteArray src, jint srcLength) {
    if (srcLength <= 0 || NULL == src) {
        return NULL;
    }

    jbyte *srcData = env->GetByteArrayElements(src, NULL);
    const char *dstData = NULL;
    int encryptLength = encryptAoeDataToData_Version1((const char *) srcData, srcLength, &dstData);
    if (encryptLength <= 0) {
        return NULL;
    }

    jbyteArray response = env->NewByteArray(encryptLength);
    env->SetByteArrayRegion(response, 0, encryptLength, (jbyte *) dstData);
    env->ReleaseByteArrayElements(src, srcData, 0);

    delete[] dstData;
    return response;
}

JNIEXPORT jint JNICALL
Java_com_didi_aoe_extensions_aoes_AoeSecurity_encryptToFile(JNIEnv *env, jclass type, jbyteArray src,
                                             jint srcLength, jstring destFilePath) {
    if (srcLength <= 0 || NULL == src || NULL == destFilePath) {
        return -1;
    }


    jbyte *srcData = env->GetByteArrayElements(src, NULL);
    const char *destPath = env->GetStringUTFChars(destFilePath, 0);
    const int ret = encryptAoeDataToFile_Version1((const char *) srcData, srcLength, destPath);
    env->ReleaseByteArrayElements(src, srcData, 0);
    env->ReleaseStringUTFChars(destFilePath, destPath);
    return ret;
}


#ifdef __cplusplus
}
#endif
