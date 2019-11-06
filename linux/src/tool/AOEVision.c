#include "AOEVision.h"
#include <stddef.h>

#include "libyuv.h"

uint8_t *convertNV21ToRGBA(const uint8_t *nv21Src, int srcWidth, int srcHeight)
{
    int responseDimens = (srcWidth * srcHeight) << 2;

    uint8_t *rgbaData = (uint8_t *)malloc(responseDimens);
    if (NULL != rgbaData)
    {
        NV21ToABGR((const uint8_t *)nv21Src, srcWidth,
                   (const uint8_t *)nv21Src + srcWidth * srcHeight,
                   srcWidth, rgbaData, srcWidth << 2, srcWidth, srcHeight);
    }

    return rgbaData;
}