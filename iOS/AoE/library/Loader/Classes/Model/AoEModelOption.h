//
//  AoEModelOption.h
//  AoE
//
//  Created by dingchao on 2019/3/20.
//

#import <Foundation/Foundation.h>
#import "AoEModelOptionProtocol.h"

@interface AoEModelOption : NSObject <AoEModelOptionProtocol, NSCopying>

/**
 模型版本
 */
@property (strong, nonatomic) NSString *version;

/**
 模型唯一索引
 */
@property (strong, nonatomic) NSString *tag;

/**
 模型文件所在文件夹
 */
@property (strong, nonatomic) NSString *modelDir;

/**
 模型名称
 */
@property (strong, nonatomic) NSString *modelName;

/**
 模型升级检查 url
 */
@property (strong, nonatomic) NSString *updateUrl;

/**
 下载后的模型地址
 */
@property (strong, nonatomic) NSString *modelPath;
/**
 模型文件签名
 */
@property (strong, nonatomic) NSString *sign;
/**
 模型是否加密
 */
@property (assign, nonatomic) BOOL encrypted;
/**
 模型加密类型
 */
@property (assign, nonatomic) NSInteger encryptType;

+ (instancetype)modelWithPath:(NSString *)path;
- (instancetype)initWithPath:(NSString *)path;
- (instancetype)initWithDictionary:(NSDictionary *)opt;

@end


@interface AoEModelOption (AoE_Serializationed)

- (NSString *)JSONSerializationedString;
- (id)objectSerializationed;
+ (id<AoEModelOptionProtocol>)ObjectFromJSONSerializationedString:(NSString *)jsonString;

+ (id<AoEModelOptionProtocol>)ObjectFromJSONSerializationedFilePath:(NSString *)filePath;

@end
