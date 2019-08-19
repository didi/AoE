//
//  AoECrypto.m
//  AoE
//
//  Created by dingchao on 2019/8/19.
//

#import "AoECryptoUtil.h"
#import "AoEValidJudge.h"
#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonDigest.h>
#import "aoecrypt.h"

#define CC_MD5_DIGEST_LENGTH    16          /* digest length in bytes */
#define VERSION 1
#define AOE_DEFAULT_KEY @"0000000000000000"
#define AOE_DEFAULT_IV @"0000000000000000"

@implementation AoECryptoUtil

+ (NSString *)aoe_encryptMD5Data:(NSData *)data {
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(data.bytes, (CC_LONG)data.length, result);
    
    return [[NSString stringWithFormat:
             @"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
             result[0], result[1], result[2], result[3],
             result[4], result[5], result[6], result[7],
             result[8], result[9], result[10], result[11],
             result[12], result[13], result[14], result[15]
             ] lowercaseString];
}

+ (NSString *)aoe_encryptStringBase64Data:(NSData *)data {
    return [data base64EncodedStringWithOptions:0];
}

+ (NSData *)aoe_encryptDataBase64Data:(NSData *)data {
    return [data base64EncodedDataWithOptions:0];
}

+ (NSData *)aoe_encryptAES128Data:(NSData *)data key:(NSString *)key iv:(NSData *)iv {
    // 'key' should be 32 bytes for AES256, will be null-padded otherwise
    const int keySize = 16;
    char keyPtr[16 + 1]; // room for terminator (unused)
    bzero( keyPtr, sizeof( keyPtr ) ); // fill with zeroes (for padding)
    
    // fetch key data
    [key getCString:keyPtr maxLength:sizeof( keyPtr ) encoding:NSUTF8StringEncoding];
    
    NSUInteger dataLength = [data length];
    
    //See the doc: For block ciphers, the output size will always be less than or
    //equal to the input size plus the size of one block.
    //That's why we need to add the size of one block here
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc( bufferSize );
    
    size_t numBytesEncrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt( kCCEncrypt, kCCAlgorithmAES128, kCCOptionPKCS7Padding,
                                          keyPtr, keySize,
                                          iv ? iv.bytes:NULL,
                                          [data bytes], dataLength, /* input */
                                          buffer, bufferSize, /* output */
                                          &numBytesEncrypted );
    if ( cryptStatus == kCCSuccess ) {
        //the returned NSData takes ownership of the buffer and will free it on deallocation
        return [NSData dataWithBytesNoCopy:buffer length:numBytesEncrypted];
    }
    
    free( buffer ); //free the buffer
    return nil;


}
+ (NSData *)aoe_dencryptAES128Data:(NSData *)data key:(NSString *)key iv:(NSData *)iv
{
    // 'key' should be 32 bytes for AES256, will be null-padded otherwise
    const int keySize = 16;
    char keyPtr[17]; // room for terminator (unused)
    bzero( keyPtr, sizeof( keyPtr ) ); // fill with zeroes (for padding)
    
    // fetch key data
    [key getCString:keyPtr maxLength:sizeof( keyPtr ) encoding:NSUTF8StringEncoding];
    
    NSUInteger dataLength = [data length];
    
    //See the doc: For block ciphers, the output size will always be less than or
    //equal to the input size plus the size of one block.
    //That's why we need to add the size of one block here
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc( bufferSize );
    
    size_t numBytesDecrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt( kCCDecrypt, kCCAlgorithmAES128, kCCOptionPKCS7Padding,
                                          keyPtr, keySize,
                                          iv ? iv.bytes:NULL,
                                          [data bytes], dataLength, /* input */
                                          buffer, bufferSize, /* output */
                                          &numBytesDecrypted );
    
    if ( cryptStatus == kCCSuccess ) {
        //the returned NSData takes ownership of the buffer and will free it on deallocation
        return [NSData dataWithBytesNoCopy:buffer length:numBytesDecrypted];
    }
    
    free( buffer ); //free the buffer
    return nil;

}

+ (NSData *)aoe_decryptAoEModel:(NSData *)soure
                     encryptKey:(NSString *)key
                         offset:(NSData *)iv
                       saltType:(AoECryptoType)type {
    if (!soure) {
        return nil;
    }
    
    NSData *distData = nil;
    if (AoECryptoTypeForAES128 == type) {
        const char *dencryptDataBytes = NULL;
        int len = dencryptAoeData(soure.bytes, soure.length, &dencryptDataBytes);
        if (len > 0 && len < soure.length) {
            NSData *dencryptData = [NSData dataWithBytes:dencryptDataBytes length:len];
            NSString *decodeKey = ([AoEValidJudge isValidString:key] ? key : AOE_DEFAULT_KEY);
            NSData *ivData = iv.length > 0 ? iv : [AOE_DEFAULT_IV dataUsingEncoding:NSUTF8StringEncoding];
            distData = [self aoe_dencryptAES128Data:dencryptData
                                                key:decodeKey
                                                 iv:ivData];
        }
    }
    return distData;
}

+ (NSData *)aoe_encryptAoEModel:(NSData *)soure
                     encryptKey:(NSString *)key
                         offset:(NSData *)iv
                       saltType:(AoECryptoType)type {
    if (!soure) {
        return nil;
    }
    NSData *distData = nil;
    if (AoECryptoTypeForAES128 == type) {
        const char *distDataBytes = NULL;
        const char *headerDataBytes = NULL;
        NSString *decodeKey = ([AoEValidJudge isValidString:key] ? key : AOE_DEFAULT_KEY);
        NSData *ivData = iv.length > 0 ? iv : [AOE_DEFAULT_IV dataUsingEncoding:NSUTF8StringEncoding];
        NSData *dencodeData = [self aoe_encryptAES128Data:soure key:decodeKey iv:ivData];
        int headerLen = getAoECryptHeader(soure.bytes, (int)soure.length, VERSION, &headerDataBytes);
        int len = encryptAoeData(dencodeData.bytes, soure.length, headerDataBytes, &distDataBytes);
        if (len == soure.length + headerLen) {
            distData = [NSData dataWithBytes:distDataBytes length:len];
        }
    }
    return distData;
}


@end
