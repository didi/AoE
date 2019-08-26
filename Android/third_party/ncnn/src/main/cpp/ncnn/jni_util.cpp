//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include "jni_utils.h"
#include <stdio.h>
#include <stdlib.h>


const char kIllegalArgumentException[] = "java/lang/IllegalArgumentException";
const char kIllegalStateException[] = "java/lang/IllegalStateException";
const char kNullPointerException[] = "java/lang/NullPointerException";
const char kIndexOutOfBoundsException[] = "java/lang/IndexOutOfBoundsException";
const char kUnsupportedOperationException[] = "java/lang/UnsupportedOperationException";

void aoencnn::jni::ThrowException(JNIEnv *env, const char *clazz, const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    const size_t max_msg_len = 512;
    auto *message = static_cast<char *>(malloc(max_msg_len));
    if (message && (vsnprintf(message, max_msg_len, fmt, args) >= 0)) {
        env->ThrowNew(env->FindClass(clazz), message);
    } else {
        env->ThrowNew(env->FindClass(clazz), "");
    }
    if (message) {
        free(message);
    }
    va_end(args);
}