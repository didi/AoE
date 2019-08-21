#include <stdbool.h>
#include <stdlib.h>
#include "aoes.h"
#include "aes.h"
#include "md5.h"

const int BUFFERSIZE = 1024 * 16;
const int MD5_SIZE = 16;
const int AOE_HEAD_SIZE = 21;

#define VERSION 1
#define AOE_KEY_VERSION1 "0000000000000000"
#define AOE_IV "0000000000000000"

int decryptAoeFile_cbc(const char *srcFile, const char *dstFile, const char *key, const char *iv) {
    if (NULL == srcFile || NULL == dstFile || NULL == key || NULL == iv) {
        return -1;
    }

    FILE *inputFile = fopen(srcFile, "r");
    if (NULL == inputFile) {
        fclose(inputFile);
        return -1;
    }

    // read version
    const int version = fgetc(inputFile);
    if (version == 1) {
        // read file head, 20 bytes
        const int headLength = 20;
        uint8_t head[headLength];
        memset(head, 0, headLength);
        fread(head, sizeof(uint8_t), headLength, inputFile);

        const int fileOrgLengh = ((head[0] << 24) & 0xFF000000) | ((head[1] << 16) & 0xFF0000) |
                                 ((head[2] << 8) & 0xFF00) | ((head[3]) & 0xFF);
        if (fileOrgLengh <= 0) {
            fclose(inputFile);
            return -1;
        }

        FILE *filewrite = fopen(dstFile, "w+");
        if (NULL == filewrite) {
            return -1;
        }

        uint8_t buffer[BUFFERSIZE];
        int readLen = 0;

        struct AES_ctx ctx;
        AES_init_ctx_iv(&ctx, (uint8_t *) key, (uint8_t *) iv);

        long readTotalSize = 0;
        bool needMix = true;

        do {
            memset(buffer, 0, BUFFERSIZE);
            readLen = fread(buffer, sizeof(uint8_t), BUFFERSIZE, inputFile);
            if (readLen <= 0) {
                break;
            }

            readTotalSize += readLen;

            if (needMix) {
                const int mixCount = readLen / 1024;
                for (int i = 0; i < mixCount; i++) {
                    uint8_t temp = buffer[i * 1024];
                    buffer[i * 1024] = head[4 + i];
                    head[4 + i] = temp;
                }
                needMix = false;
            }

            AES_CBC_decrypt_buffer(&ctx, buffer, readLen);
            if (readTotalSize < fileOrgLengh) {
                fwrite(buffer, sizeof(uint8_t), readLen, filewrite);
            } else {
                const int writeLen = (int) (readLen - (readTotalSize - fileOrgLengh));
                fwrite(buffer, sizeof(uint8_t), writeLen, filewrite);
            }
        } while (readLen > 0);

        fclose(filewrite);
        fclose(inputFile);

        return 0;
    }

    fclose(inputFile);
    return -1;
}

int decryptAoeFile_cbc_mem(const char *srcFile, char **dstMem, const char *key, const char *iv) {
    if (NULL == srcFile || NULL == key || NULL == iv) {
        return -1;
    }

    FILE *inputFile = fopen(srcFile, "r");
    if (NULL == inputFile) {
        fclose(inputFile);
        return -1;
    }

    // read version
    const int version = fgetc(inputFile);
    if (version == 1) {
        // read file head, 20 bytes
        const int headLength = 20;
        uint8_t head[headLength];
        memset(head, 0, headLength);
        fread(head, sizeof(uint8_t), headLength, inputFile);

        const int fileOrgLengh = ((head[0] << 24) & 0xFF000000) | ((head[1] << 16) & 0xFF0000) |
                                 ((head[2] << 8) & 0xFF00) | ((head[3]) & 0xFF);
        if (fileOrgLengh <= 0) {
            fclose(inputFile);
            return -1;
        }

        char *response = malloc(fileOrgLengh);
        if (NULL == response) {
            return -1;
        }

        char *flag = response;
        uint8_t buffer[BUFFERSIZE];
        int readLen = 0;

        struct AES_ctx ctx;
        AES_init_ctx_iv(&ctx, (uint8_t *) key, (uint8_t *) iv);

        long readTotalSize = 0;
        bool needMix = true;

        do {
            memset(buffer, 0, BUFFERSIZE);
            readLen = fread(buffer, sizeof(uint8_t), BUFFERSIZE, inputFile);
            if (readLen <= 0) {
                break;
            }

            readTotalSize += readLen;

            if (needMix) {
                const int mixCount = readLen / 1024;
                for (int i = 0; i < mixCount; i++) {
                    uint8_t temp = buffer[i * 1024];
                    buffer[i * 1024] = head[4 + i];
                    head[4 + i] = temp;
                }
                needMix = false;
            }

            AES_CBC_decrypt_buffer(&ctx, buffer, readLen);
            if (readTotalSize < fileOrgLengh) {
                memcpy(flag, buffer, readLen);
                flag += readLen;
            } else {
                const int writeLen = (int) (readLen - (readTotalSize - fileOrgLengh));
                memcpy(flag, buffer, writeLen);
                flag += writeLen;
            }
        } while (readLen > 0);

        fclose(inputFile);

        *dstMem = response;
        return 0;
    }

    fclose(inputFile);
    return -1;
}

int encryptAoeFile_Version1(const char *srcFile, const char *dstFile) {
    if (NULL == srcFile || NULL == dstFile) {
        return -1;
    }

    int fileOrgSize = 0;
    FILE *file = fopen(srcFile, "r");
    if (NULL == file) {
        return -1;
    }

    FILE *filewrite = fopen(dstFile, "w+");
    if (NULL == filewrite) {
        return -1;
    }

    const int headLength = 21;
    uint8_t header[21];
    memset(header, 0, headLength);
    header[0] = VERSION;

    unsigned char fileMd5[MD5_SIZE];
    MD5_CTX md5;
    MD5Init(&md5);

    uint8_t buffer[BUFFERSIZE];
    int nLen = 0;

    do {
        memset(buffer, 0, BUFFERSIZE);
        nLen = fread(buffer, sizeof(uint8_t), BUFFERSIZE, file);
        if (nLen <= 0) {
            break;
        }

        fileOrgSize += nLen;
        MD5Update(&md5, buffer, nLen);
    } while (nLen > 0);

    MD5Final(&md5, fileMd5);

    header[1] = fileOrgSize >> 24 & 0xff;
    header[2] = fileOrgSize >> 16 & 0xff;
    header[3] = fileOrgSize >> 8 & 0xff;
    header[4] = fileOrgSize & 0xff;

    fseek(file, 0, SEEK_SET);
    struct AES_ctx ctx;
    AES_init_ctx_iv(&ctx, (uint8_t *) AOE_KEY_VERSION1, (uint8_t *) AOE_IV);

    bool needMix = true;
    do {
        memset(buffer, 0, BUFFERSIZE);
        nLen = fread(buffer, sizeof(uint8_t), BUFFERSIZE, file);
        if (nLen <= 0) {
            break;
        }

        int dataLen = nLen;
        if (nLen % AES_BLOCKLEN != 0) {
            dataLen = nLen + (AES_BLOCKLEN - nLen % AES_BLOCKLEN);
        }

        AES_CBC_encrypt_buffer(&ctx, buffer, dataLen);
        if (needMix) {
            const int mixCount = dataLen / 1024;
            for (int i = 0; i < mixCount; i++) {
                uint8_t temp = buffer[i * 1024];
                buffer[i * 1024] = fileMd5[i];
                fileMd5[i] = temp;
            }

            unsigned char *md5Point = &header[5];
            memcpy(md5Point, fileMd5, MD5_SIZE);

            fseek(filewrite, 0, SEEK_SET);
            fwrite(header, sizeof(uint8_t), headLength, filewrite);

            needMix = false;
        }
        fwrite(buffer, sizeof(uint8_t), dataLen, filewrite);

    } while (nLen > 0);

    fclose(filewrite);
    fclose(file);

    return fileOrgSize;
}

int encryptAoeDataToFile_Version1(const char *srcData, const int srcLength, const char *dstFile) {
    if (NULL == srcData || NULL == dstFile || srcLength <= 0) {
        return -1;
    }

    int fileOrgSize = srcLength;

    FILE *filewrite = fopen(dstFile, "w+");
    if (NULL == filewrite) {
        return -1;
    }

    int dstDataLength = 21 + srcLength;
    if (srcLength % AES_BLOCKLEN != 0) {
        dstDataLength += (AES_BLOCKLEN - srcLength % AES_BLOCKLEN);
    }

    const int headLength = 21;
    uint8_t header[21];
    memset(header, 0, headLength);
    header[0] = VERSION;

    unsigned char fileMd5[MD5_SIZE];
    MD5_CTX md5;
    MD5Init(&md5);

    uint8_t buffer[BUFFERSIZE];
    int nLen = 0;

    int left = srcLength;
    const char *dataPos = srcData;

    while (left > 0) {
        memset(buffer, 0, BUFFERSIZE);
        nLen = (left < BUFFERSIZE) ? left : BUFFERSIZE;

        memcpy(buffer, dataPos, nLen);
        MD5Update(&md5, buffer, nLen);
        left -= nLen;
        dataPos += nLen;
    }

    MD5Final(&md5, fileMd5);

    header[1] = (uint8_t)(fileOrgSize >> 24 & 0xff);
    header[2] = (uint8_t)(fileOrgSize >> 16 & 0xff);
    header[3] = (uint8_t)(fileOrgSize >> 8 & 0xff);
    header[4] = (uint8_t)(fileOrgSize & 0xff);

    dataPos = srcData;
    left = srcLength;

    struct AES_ctx ctx;
    AES_init_ctx_iv(&ctx, (uint8_t *) AOE_KEY_VERSION1, (uint8_t *) AOE_IV);

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

        AES_CBC_encrypt_buffer(&ctx, buffer, dataLen);
        if (needMix) {
            const int mixCount = dataLen / 1024;
            for (int i = 0; i < mixCount; i++) {
                uint8_t temp = buffer[i * 1024];
                buffer[i * 1024] = fileMd5[i];
                fileMd5[i] = temp;
            }

            unsigned char *md5Point = &header[5];
            memcpy(md5Point, fileMd5, MD5_SIZE);

            fseek(filewrite, 0, SEEK_SET);
            fwrite(header, sizeof(uint8_t), headLength, filewrite);

            needMix = false;
        }
        fwrite(buffer, sizeof(uint8_t), dataLen, filewrite);
    }

    fclose(filewrite);

    return dstDataLength;
}

int encryptAoeDataToData_Version1(const char *srcData, const int srcLength, const char **dstData) {
    if (NULL == srcData || NULL == dstData || srcLength <= 0) {
        return -1;
    }

    int dstDataLength = 21 + srcLength;
    if (srcLength % AES_BLOCKLEN != 0) {
        dstDataLength += (AES_BLOCKLEN - srcLength % AES_BLOCKLEN);
    }

    char *response = malloc(dstDataLength);
    if (NULL == response) {
        return -1;
    }

    char *writePos = response;
    const int fileOrgSize = srcLength;
    const int headLength = AOE_HEAD_SIZE;
    uint8_t header[AOE_HEAD_SIZE];
    memset(header, 0, headLength);
    header[0] = VERSION;

    unsigned char fileMd5[MD5_SIZE];
    MD5_CTX md5;
    MD5Init(&md5);

    uint8_t buffer[BUFFERSIZE];
    int nLen = 0;

    int left = srcLength;
    const char *dataPos = srcData;

    while (left > 0) {
        memset(buffer, 0, BUFFERSIZE);
        nLen = (left < BUFFERSIZE) ? left : BUFFERSIZE;

        memcpy(buffer, dataPos, nLen);
        MD5Update(&md5, buffer, nLen);
        left -= nLen;
        dataPos += nLen;
    }

    MD5Final(&md5, fileMd5);

    header[1] = (uint8_t)(fileOrgSize >> 24 & 0xff);
    header[2] = (uint8_t)(fileOrgSize >> 16 & 0xff);
    header[3] = (uint8_t)(fileOrgSize >> 8 & 0xff);
    header[4] = (uint8_t)(fileOrgSize & 0xff);

    dataPos = srcData;
    left = srcLength;

    struct AES_ctx ctx;
    AES_init_ctx_iv(&ctx, (uint8_t *) AOE_KEY_VERSION1, (uint8_t *) AOE_IV);

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

        AES_CBC_encrypt_buffer(&ctx, buffer, dataLen);
        if (needMix) {
            const int mixCount = dataLen / 1024;
            for (int i = 0; i < mixCount; i++) {
                uint8_t temp = buffer[i * 1024];
                buffer[i * 1024] = fileMd5[i];
                fileMd5[i] = temp;
            }

            unsigned char *md5Point = &header[5];
            memcpy(md5Point, fileMd5, MD5_SIZE);

            memcpy(writePos, header, headLength);
            writePos += headLength;

            needMix = false;
        }

        memcpy(writePos, buffer, dataLen);
        writePos += dataLen;
    }

    *dstData = response;
    return dstDataLength;
}
