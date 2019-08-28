//
//  AoEModelConfig.m
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import "AoEModelConfig.h"
#import "AoEValidJudge.h"

@implementation AoEModelConfig

+ (NSString *)ModelConfigFileExtension {
    return @"config";
}

+ (NSString *)ModelConfigFileName {
    return @"model";
}

+ (NSString *)ModelFileExtension {
    return @"bin";
}

+ (NSString *)ModelsDirLocalPath {
    // 为支持单tag多模型的存储结构 一个tag下根据多个目录存储多个模型
    NSString *modelsStoragePath = [[self SDKLocalPath] stringByAppendingPathComponent:@"Model"];
    return modelsStoragePath;
}

+ (NSString *)SDKLocalPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *homePath = [paths firstObject];
    // 为支持单tag多模型的存储结构 一个tag下根据多个目录存储多个模型
    NSString *homeStoragePath = [homePath stringByAppendingPathComponent:@"AoE"];
    return homeStoragePath;
}

+ (NSString *)modelLocalPathForTag:(NSString *)tag alias:(NSString *)alias {
    NSString *modelStoragePath = [[self ModelsDirLocalPath] stringByAppendingPathComponent:tag];
    if ([AoEValidJudge isValidString:alias]) {
        modelStoragePath = [modelStoragePath stringByAppendingPathComponent:alias];
    }
    BOOL isDirectory = NO;
    if (![[NSFileManager defaultManager] fileExistsAtPath:modelStoragePath isDirectory:&isDirectory]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:modelStoragePath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    return modelStoragePath;
}

@end
