#ifndef PROJECT_AOES_H
#define PROJECT_AOES_H

#include <stdio.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

int decryptAoeFile_cbc(const char *srcFile, const char *dstFile, const char *key, const char *iv);

// 解密到内存里，dstMem需要外部free
int decryptAoeFile_cbc_mem(const char *srcFile, char **dstMem, const char *key, const char *iv);

/**
 * 加密文件
 * @param srcFile 原始文件
 * @param dstFile 加密后的文件
 * @return 原始文件长度
 */
int encryptAoeFile_Version1(const char *srcFile, const char *dstFile);

/**
 * 加密内存数据到dstFile文件
 * @param srcData 原始数据
 * @param srcLength 原始数据长度
 * @param dstFile 加密文件路径
 * @return 加密后的长度，返回-1表示加密失败
 */
int encryptAoeDataToFile_Version1(const char *srcData, const int srcLength, const char *dstFile);

/**
 * 加密内存数据到dstData
 * @param srcData 原始数据
 * @param srcLength 原始数据长度
 * @param dstData 加密后的数据，需要外部释放
 * @return 加密后的长度，返回-1表示加密失败
 */
int encryptAoeDataToData_Version1(const char *srcData, const int srcLength, const char **dstData);

#ifdef __cplusplus
}
#endif

#endif //PROJECT_AOES_H
