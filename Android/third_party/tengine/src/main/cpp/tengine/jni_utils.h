//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_JNI_UTILS_H
#define AOE_ANDROID_JNI_UTILS_H

#include <jni.h>

#include <android/log.h>

#define TAG "AOE_TENGINE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

extern const char kIllegalArgumentException[];
extern const char kIllegalStateException[];
extern const char kNullPointerException[];
extern const char kIndexOutOfBoundsException[];
extern const char kUnsupportedOperationException[];

namespace aoetengine {

    namespace jni {
        void ThrowException(JNIEnv *env, const char *clazz, const char *fmt, ...);
    }
}

#endif //AOE_ANDROID_JNI_UTILS_H
