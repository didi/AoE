//
//  AoEFundationUtil.m
//  AoE
//
//  Created by dingchao on 2019/5/11.
//

#import "AoEFoundationUtil.h"
#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonDigest.h>


#define CC_MD5_DIGEST_LENGTH    16          /* digest length in bytes */

@implementation AoEFoundationUtil

+ (NSString *)aoe_MD5Data:(NSData *)data {
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

+ (NSString *)aoe_Base64Data:(NSData *)data {
    return [data base64EncodedStringWithOptions:0];
}

@end
