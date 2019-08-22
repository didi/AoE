#ifndef PROJECT_AOECRYPT_H
#define PROJECT_AOECRYPT_H

#include <stdio.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif
    
struct aoecrypt_header
{
    uint8_t fileSize[4];
    uint8_t version;
    char *md5;
};

/**
* 加密内存数据到dstData
* @param srcData 原始数据
* @param srcLength 原始数据长度
* @param header 头数据
* @param dstData 加密后的数据，需要外部释放
* @return 加密后的长度，返回-1表示加密失败
*/
int encryptAoeData(const char *srcData, const int srcLength, const char *header, const char **dstData);
    
/**
* 解密内存数据到dstData
* @param srcData 原始数据
* @param srcLength 原始数据长度
* @param dstData 加密后的数据，需要外部释放
* @return 加密后的长度，返回-1表示加密失败
*/
int dencryptAoeData(const char *srcData, const int srcLength, const char **dstData);

/**
 获取AoE header 信息

 @param srcData 原始数据
 @param srcLength 原始数据长度
 @param verion 版本号
 @param header 根据原始数据获得到 头数据
 @return 头数据长度，返回-1表示加密失败
 */
int getAoECryptHeader(const char *srcData, const int srcLength, const int verion, const char **header);

#ifdef __cplusplus
}
#endif

#endif //PROJECT_AOECRYPT_H
