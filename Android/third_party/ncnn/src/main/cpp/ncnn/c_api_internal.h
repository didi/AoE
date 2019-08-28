//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_C_API_INTERNAL_H
#define AOE_ANDROID_C_API_INTERNAL_H

#include <stdint.h>

typedef struct {
    float re, im;  // real and imaginary parts, respectively.
} NcnnComplex64;

// Half precision data type compatible with the C99 definition.
typedef struct {
    uint16_t data;
} NcnnFloat16;


// Types supported by ncnn
typedef enum {
    kNcnnNoType = 0,
    kNcnnFloat32 = 1,
    kNcnnInt32 = 2,
    kNcnnUInt8 = 3,
    kNcnnInt64 = 4,
    kNcnnString = 5,
    kNcnnBool = 6,
    kNcnnInt16 = 7,
    kNcnnComplex64 = 8,
    kNcnnInt8 = 9,
    kNcnnFloat16 = 10,
} NcnnType;


#endif //AOE_ANDROID_C_API_INTERNAL_H
