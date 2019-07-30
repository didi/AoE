//
//  AEModelConfigProtocol.h
//  AoE-iOS
//
//  Created by dingchao on 2019/6/10.
//

#import <Foundation/Foundation.h>

@protocol AoEModelLoaderConfigProtocol <NSObject>


/**
 sdk 本地存储位置

 @return 本地存储位置
 */
+ (NSString *)SDKLocalPath;

/**
 模型存储位置

 @return 模型存储位置
 */
+ (NSString *)ModelsDirLocalPath;

/**
 单个模型存储位置

 @param tag 模型存储唯一值
 @param alias 别名
 @return 单个模型存储位置
 */
+ (NSString *)modelLocalPathForTag:(NSString *)tag alias:(NSString *)alias;

/**
 模型配置文件名

 @return 模型配置文件名
 */
+ (NSString *)ModelConfigFileName;

/**
 模型配置扩展名

 @return 模型配置扩展名
 */
+ (NSString *)ModelConfigFileExtension;

/**
 模型扩展名

 @return 模型扩展名
 */
+ (NSString *)ModelFileExtension;
@end
