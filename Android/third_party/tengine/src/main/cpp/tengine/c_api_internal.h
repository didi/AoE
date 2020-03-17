//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_C_API_INTERNAL_H
#define AOE_ANDROID_C_API_INTERNAL_H

#include <stdint.h>

#include "jni_utils.h"

using aoetengine::jni::ThrowException;

//#define TENGINE_DT_FP32 0
//#define TENGINE_DT_FP16 1
//#define TENGINE_DT_INT8 2
//#define TENGINE_DT_UINT8 3
//#define TENGINE_DT_INT32 4
//#define TENGINE_DT_INT16 5

// Types supported by tengine
typedef enum {
    kTengineFloat32 = 0,
    kTengineFloat16 = 1,
    kTengineInt8 = 2,
    kTengineUInt8 = 3,
    kTengineInt32 = 4,
    kTengineInt16 = 5,
} TengineType;

#endif //AOE_ANDROID_C_API_INTERNAL_H
