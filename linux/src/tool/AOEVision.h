#include "stdint.h"

// 把NV21图像数据转换成RGBA
uint8_t* convertNV21ToRGBA(const uint8_t* nv21Src, int srcWidth, int srcHeight); 