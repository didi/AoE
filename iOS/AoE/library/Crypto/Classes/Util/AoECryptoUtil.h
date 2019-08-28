//
//  AoECrypto.h
//  AoE
//
//  Created by dingchao on 2019/8/19.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, AoECryptoType) {
    AoECryptoTypeForAES128,
};

@interface AoECryptoUtil : NSObject

/**
 md5加密

 @param data 需要加密数据
 @return 加密后的结果
 */
+ (NSString *)aoe_encryptMD5Data:(NSData *)data;

/**
 base64加密方法

 @param data 需要加密数据
 @return 加密后的结果string 或者 data
 */
+ (NSString *)aoe_encryptStringBase64Data:(NSData *)data;
+ (NSData *)aoe_encryptDataBase64Data:(NSData *)data;


/**
 AES 128 cbc加解密方法

 @param data 需要加密数据
 @param key 加密key
 @param iv 偏移量
 @return 加解密后的结果
 */
+ (NSData *)aoe_encryptAES128Data:(NSData *)data key:(NSString *)key iv:(NSData *)iv;
+ (NSData *)aoe_decryptAES128Data:(NSData *)data key:(NSString *)key iv:(NSData *)iv;


/**
 模型cbc加密方法
 
 @param data 未加密模型文件数据
 @param type 加密类型
 @param key 秘钥
 @param iv 偏移量
 @return 加密后模型数据
 */
+ (NSData *)aoe_encryptAoEModel:(NSData *)data
                     encryptKey:(NSString *)key
                         offset:(NSData *)iv
                       saltType:(AoECryptoType)type;

/**
 模型cbc解密方法
 
 @param data 加密模型文件数据
 @param key 秘钥
 @param iv 偏移量
 @param type 加密类型
 @return 解密模型数据
 */
+ (NSData *)aoe_decryptAoEModel:(NSData *)data
                     encryptKey:(NSString *)key
                         offset:(NSData *)iv
                       saltType:(AoECryptoType)type;
@end
