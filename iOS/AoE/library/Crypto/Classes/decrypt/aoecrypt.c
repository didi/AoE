#include <stdbool.h>
#include <stdlib.h>
#include "aoecrypt.h"
#include "aoeaes.h"
#include "aoemd5.h"

const int BUFFERSIZE = 1024 * 16;
const int MD5_SIZE = 16;
const int AOE_HEAD_SIZE = 21;

int getAoEHeaderSize(int version) {
    int headerSize = 0;
    if (version == 1) {
        headerSize = AOE_HEAD_SIZE;
    }
    return headerSize;
}

int getAoECryptHeader(const char *srcData, const int srcLength,const int version, const char **header) {
    if (NULL == srcData ||
        NULL == header ||
        version < 0 ||
        srcLength <= 0) {
        return -1;
    }
    const int headLength = getAoEHeaderSize(version);
    char *response = malloc(headLength);
    if (NULL == response) {
        return -1;
    }
    
    const int fileOrgSize = srcLength;
    uint8_t tempHeader[headLength];
    memset(tempHeader, 0, headLength);
    tempHeader[0] = version;
    
    unsigned char fileMd5[MD5_SIZE];
    aoe_MD5_CTX md5;
    aoe_MD5Init(&md5);
    
    uint8_t buffer[BUFFERSIZE];
    int nLen = 0;
    
    int left = srcLength;
    const char *dataPos = srcData;
    
    while (left > 0) {
        memset(buffer, 0, BUFFERSIZE);
        nLen = (left < BUFFERSIZE) ? left : BUFFERSIZE;
        
        memcpy(buffer, dataPos, nLen);
        aoe_MD5Update(&md5, buffer, nLen);
        left -= nLen;
        dataPos += nLen;
    }
    
    aoe_MD5Final(&md5, fileMd5);
    
    tempHeader[1] = (uint8_t) (fileOrgSize >> 24 & 0xff);
    tempHeader[2] = (uint8_t) (fileOrgSize >> 16 & 0xff);
    tempHeader[3] = (uint8_t) (fileOrgSize >> 8 & 0xff);
    tempHeader[4] = (uint8_t) (fileOrgSize & 0xff);
    
    unsigned char *md5Point = &tempHeader[5];
    memcpy(md5Point, fileMd5, MD5_SIZE);
    
    memcpy(response, tempHeader, headLength);
    *header = response;
    return srcLength;
}

int getAoECryptedHeader(const char *srcData, int srcLength, const char **header, const int *fileSize) {
    if (NULL == srcData ||
        NULL == header) {
        return -1;
    }
    const int version = srcData[0];
    const int headLength = getAoEHeaderSize(version);
    if (srcLength < headLength ||
        headLength <= 0) {
        return -1;
    }
    
    char *response = malloc(headLength);
    if (NULL == response) {
        return -1;
    }
    
    uint8_t tempHeader[headLength];
    memset(tempHeader, 0, headLength);
    memcpy(tempHeader, srcData, headLength);
    
    const int fileOrgSize = ((tempHeader[0] << 24) & 0xFF000000) |
                            ((tempHeader[1] << 16) & 0xFF0000) |
                            ((tempHeader[2] << 8) & 0xFF00) |
                            ((tempHeader[3]) & 0xFF);
    if (fileOrgSize <= 0) {
        return -1;
    }
    
    memcpy(response, tempHeader, headLength);
    *header = response;
    fileSize = &fileOrgSize;
    return headLength;
}

int encryptAoeData(const char *srcData, const int srcLength, const char *header, const char **dstData) {
    if (NULL == srcData || NULL == dstData || srcLength <= 0) {
        return -1;
    }
    const int version = srcData[0];
    const int headLength = getAoEHeaderSize(version);
    if (headLength <= 0) {
        return -1;
    }
    
    int dstDataLength = headLength + srcLength;
    if (srcLength % AES_BLOCKLEN != 0) {
        dstDataLength += (AES_BLOCKLEN - srcLength % AES_BLOCKLEN);
    }
    
    char *response = malloc(dstDataLength);
    if (NULL == response) {
        return -1;
    }
    
    char *writePos = response;
    unsigned char fileMd5[MD5_SIZE];
    uint8_t buffer[BUFFERSIZE];
    int nLen = 0;
    int left = srcLength;
    const char *dataPos = srcData;
    bool needMix = true;
    
    while (left > 0) {
        memset(buffer, 0, BUFFERSIZE);
        
        nLen = (left < BUFFERSIZE) ? left : BUFFERSIZE;
        memcpy(buffer, dataPos, nLen);
        left -= nLen;
        dataPos += nLen;
        
        int dataLen = nLen;
        if (nLen % AES_BLOCKLEN != 0) {
            dataLen = nLen + (AES_BLOCKLEN - nLen % AES_BLOCKLEN);
        }
        
        if (needMix) {
            const int mixCount = dataLen / 1024;
            for (int i = 0; i < mixCount; i++) {
                uint8_t temp = buffer[i * 1024];
                buffer[i * 1024] = fileMd5[i];
                fileMd5[i] = temp;
            }
            
            memcpy(writePos, header, headLength - MD5_SIZE);
            writePos += headLength - MD5_SIZE;
            memcpy(writePos, fileMd5, MD5_SIZE);
            needMix = false;
        }
        
        memcpy(writePos, buffer, dataLen);
        writePos += dataLen;
    }
    
    *dstData = response;
    return dstDataLength;
}

int dencryptAoeData(const char *srcData, const int srcLength, const char **dstData) {
    if (NULL == srcData || NULL == dstData || srcLength <= 0) {
        return -1;
    }
    
    const char *head;
    const int fileLength;
    const int headLength = getAoECryptedHeader(srcData, srcLength, &head, &fileLength);
    uint8_t header[headLength];
    if (headLength <= 0) {
        return -1;
    }
    memset(header, 0, headLength);
    memcpy(header, head, headLength);
    
    int dstDataLength = srcLength - headLength;
    char *response = malloc(dstDataLength);
    if (NULL == response) {
        return -1;
    }
    
    char *writePos = response;
    
    uint8_t buffer[BUFFERSIZE];
    int nLen = headLength;
    int left = srcLength;
    const char *dataPos = &srcData[headLength];
    bool needMix = true;
    
    while (left > 0) {
        memset(buffer, 0, BUFFERSIZE);
        
        nLen = (left < BUFFERSIZE) ? left : BUFFERSIZE;
        memcpy(buffer, dataPos, nLen);
        left -= nLen;
        dataPos += nLen;
        
        int dataLen = nLen;
        
        if (needMix) {
            const int mixCount = dataLen / 1024;
            for (int i = 0; i < mixCount; i++) {
                uint8_t temp = buffer[i * 1024];
                buffer[i * 1024] = header[headLength - MD5_SIZE - 1 + i];
                header[headLength - MD5_SIZE - 1 + i] = temp;
            }
            needMix = false;
        }
        memcpy(writePos, buffer, dataLen);
        writePos += dataLen;
    }
    *dstData = response;
    
    return dstDataLength;
}
