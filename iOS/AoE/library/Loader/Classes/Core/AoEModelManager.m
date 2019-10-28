//
//  AoEModelManager.m
//  AoE
//
//  Created by dingchao on 2019/3/22.
//

#import "AoEModelManager.h"

#import "AoEValidJudge.h"
#import "AoEModelConfig.h"
#import "AoEModelOption.h"
#import "AoEFoundationUtil.h"
#import "AoEFileManager.h"
#import "AoEVersionUtil.h"
#import "AoECryptoUtil.h"
#import "AoEUpgradeService.h"
#if __has_include("AoElogger.h")
#import "AoElogger.h"
#endif


static NSString *const AEModelManagerLocker = @"AEModelManagerLockObj";
@interface AoEModelManager ()

@property (nonatomic, strong) NSMutableDictionary *cacheModelDic;
@property (nonatomic, strong) id<AoELoggerComponentProtocol> logger;
@end

@implementation AoEModelManager

#pragma mark - public

+ (AoEModelManager *)shareInstance {
    static AoEModelManager *instance = nil;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        instance = [[AoEModelManager alloc] init];
    });
    
    return instance;
}

- (id<AoEModelOptionProtocol>)loadModelConfig:(NSString *)dir {
    return [self loadModelConfig:dir ext:nil];
}

- (id<AoEModelOptionProtocol>)loadModelConfig:(NSString *)dir ext:(NSDictionary *)extension {
    AoEModelOption *option = nil;
    option = [self modelWithModelPath:dir];
    if (!option ||
        ![option isValidOption]) {
        [[self loggerComponent] warningLog:@"installed option is not valid"];
    }
    if ([self isRemoteModelReady:option]) {
        option = [self loadRemoteModelConfig:option];
    }
    option.appId = ((NSNumber *)extension[@"appid"]).integerValue;
    option.lat = extension[@"lat"];
    option.lng = extension[@"lng"];
    [self checkUpgradeModelWithConfig:option];
    return option;
}

#pragma mark - private

- (id<AoELoggerComponentProtocol>)loggerComponent {
    if (!self.logger) {
#if __has_include("AoElogger.h")
        self.logger = [[AoElogger alloc] initWithTag:@"LoaderComponentTag"];
#endif
    }
    return self.logger;
}

- (AoEModelOption *)modelWithModelPath:(NSString *)path {
    
    if (![self getModelPathWithModelDir:path]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"getModelPathWithModelDir fail"]];
        return nil;
    }
    
    NSString *configPath = [path stringByAppendingPathComponent:[self modelConfigFile]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:configPath]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@" %@ file not exist",configPath]];
        return nil;
    }
    AoEModelOption *modelOption = nil;
    if ([AoEValidJudge isValidString:self.modelOptionClassName] &&
        [[NSClassFromString(self.modelOptionClassName) new] isKindOfClass:[AoEModelOption class]]) {
        Class optionClass = NSClassFromString(self.modelOptionClassName);
        modelOption = (AoEModelOption *)[optionClass ObjectFromJSONSerializationedFilePath:configPath];
    }else {
        modelOption = (AoEModelOption *)[AoEModelOption ObjectFromJSONSerializationedFilePath:configPath];
    }
    
    modelOption.modelPath = path;
    return modelOption;
}

- (NSString *)getModelPathWithModelDir:(NSString *)dir {
    NSString *configPath = [dir stringByAppendingPathComponent:[self modelConfigFile]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:configPath]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@" %@ file not exist",configPath]];
        return nil;
    }
    
    NSData *data = [NSData dataWithContentsOfFile:configPath];
    if (!data) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@" %@ data is valid",configPath]];
        return nil;
    }
    NSError *error = nil;
    NSDictionary *config = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
    NSString *modelName = config[@"modelName"];
    if (!modelName) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@" %@ data not have model name",configPath]];
        return nil;
    }
    
    NSString *modelPath = dir;
    BOOL isDir = NO;
    BOOL isExist = [[NSFileManager defaultManager] fileExistsAtPath:modelPath isDirectory:&isDir];
    if (!isExist || !isDir) {
        return nil;
    }
    
    return modelPath;
}

- (BOOL)isRemoteModelReady:(AoEModelOption *)option {
    BOOL isRemoteModelReady = YES;
    NSString *remoteModelPath = [self getRemoteModelPathForTag:option.tag alias:option.modelDir];
    NSString *newestVerion = [self getNewestVerion:remoteModelPath];
    isRemoteModelReady = [AoEValidJudge isValidString:newestVerion];
    return isRemoteModelReady;
}

- (AoEModelOption *)loadRemoteModelConfig:(AoEModelOption *)option {
    AoEModelOption *modelOption = self.cacheModelDic[[self keyFromOption:option]];
    // 为支持单tag多模型的存储结构 一个tag下根据多个目录存储多个模型
    NSString *remoteModelPath = [self getRemoteModelPathForTag:option.tag alias:option.modelDir];
    
    if (modelOption) {
        return modelOption;
    }
    
    //未使用过此模型，则使用最新版本模型
    NSString *lastVersion = [AoEVersionUtil findLastVersionFromVersions:[AoEFileManager fetchFilesWithPath:remoteModelPath]];
    NSString *modelPath = [self newestRemoteModelVersionPath:remoteModelPath alias:option.modelDir];
    AoEModelOption *newModelOption = [self modelWithModelPath:modelPath];
    modelOption = newModelOption;
    modelOption.version = lastVersion;
    modelOption.modelPath = modelPath;
    
    if ([AoEValidJudge isValidString:option.modelPath]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"model path can't be nil %@",modelOption.modelPath]];
        NSAssert([AoEValidJudge isValidString:option.modelPath], @"model path can't be nil");
        return nil;
    }
    @synchronized (AEModelManagerLocker) {
        [self.cacheModelDic setObject:modelOption forKey:[self keyFromOption:option]];
    }
    return modelOption;
}

- (void)checkUpgradeModelWithConfig:(AoEModelOption *)option {
    if ([option isValidOption] &&
        option.appId > 0) {
        // 升级管理的key修改为{tag + 目录}的形式，支持单tag多模型的形式。
        AoEUpgradeServiceInputModel *inputModel = [AoEUpgradeServiceInputModel initWithModuleOption:option appKey:option.appId  storagePath:[self getRemoteModelPathForTag:option.tag alias:option.modelDir]];
        inputModel.checkUpgradeModel = [self checkUpgradeClass];
        inputModel.needDownloadImmediately = (![self isInternalModelReady:option] &&
                                              ![self isValidJudgeModelExist:option]);
        [[AoEUpgradeService shareInstance] startUpgradeService:inputModel];
    }
}

- (BOOL)isNewerRemoteModel:(AoEModelOption *)remoteOption internalModel:(AoEModelOption *)option
    {
    return [AoEVersionUtil version:remoteOption.version isGreaterThanVersion:option.version];
}

- (BOOL)isInternalModelReady:(AoEModelOption *)option {
    BOOL isInternalModelReady = NO;
    if (option.modelPath) {
        NSString *fileName = [option.modelName stringByAppendingPathExtension:[self modelFileExtenstion]];
        NSString *modelFilePath = [option.modelPath stringByAppendingPathComponent:fileName];
        if ([[NSFileManager defaultManager] fileExistsAtPath:modelFilePath]) {
            isInternalModelReady = YES;
        }
    }
    return isInternalModelReady;
}

- (BOOL)isValidJudgeModelExist:(AoEModelOption *)option {
    BOOL isValidJudgeModelExist = NO;
    if (option.modelPath) {
        NSString *modelFilePath = [option.modelPath stringByAppendingPathComponent:[option.modelName stringByAppendingPathExtension:[self modelFileExtenstion]]];
        if ([[NSFileManager defaultManager] fileExistsAtPath:modelFilePath]) {
            NSData *data = [NSData dataWithContentsOfFile:modelFilePath];
            NSString *md5 = [AoECryptoUtil aoe_encryptMD5Data:data];
            if ([AoEValidJudge isValidString:md5] &&
                [md5 isEqualToString:option.sign]) {
                isValidJudgeModelExist = YES;
            }
        }
    }
    return isValidJudgeModelExist;
}

- (NSString *)newestRemoteModelVersionPath:(NSString *)remoteModelDir alias:(NSString *)alias {
    NSString *lastVersion = [self getNewestVerion:remoteModelDir];
    // 本地存储路径格式：AoE/Model/{tag}/{modelDir}/{version}/{model dir}
    NSString *modelDir = [remoteModelDir stringByAppendingPathComponent:lastVersion];
    modelDir = [modelDir stringByAppendingPathComponent:alias];
    return [self getModelPathWithModelDir:modelDir];
}

- (NSString *)getNewestVerion:(NSString *)remoteModelDir {
    NSArray *versions = [AoEFileManager fetchFilesWithPath:remoteModelDir];
    return [AoEVersionUtil findLastVersionFromVersions:versions];
}

- (NSString *)keyFromOption:(AoEModelOption *)option {
    return [option.tag stringByAppendingFormat:@"_%@",option.modelDir] ? : @"";
}

#pragma mark - path tool method

- (NSString *)modelConfigFile {
    NSString *extension = [[self modelLoaderConfigClass] ModelConfigFileExtension];
    NSString *fileName = [[self modelLoaderConfigClass] ModelConfigFileName];
    return [fileName stringByAppendingPathExtension:extension];
}

- (NSString *)getSDKHomeStoragePath {
    return [[self modelLoaderConfigClass] SDKLocalPath];
}

- (NSString *)getSDKModelsStoragePath {
    return [[self modelLoaderConfigClass] ModelsDirLocalPath];
}

- (NSString *)getRemoteModelPathForTag:(NSString *)tag alias:(NSString *)alias {
    return [[self modelLoaderConfigClass] modelLocalPathForTag:tag alias:alias];
}

- (NSString *)modelFileExtenstion {
    return [[self modelLoaderConfigClass] ModelFileExtension];
}

- (Class)modelLoaderConfigClass {
    return self.modelLoaderConfigClassName ? NSClassFromString(self.modelLoaderConfigClassName) : [AoEModelConfig class];
}

#pragma mark - getter & setter

- (NSString *)checkUpgradeClass {
    return @"AoECheckUpgradeModel";
}

- (NSMutableDictionary *)cacheModelDic {
    if (!_cacheModelDic) {
        _cacheModelDic = [NSMutableDictionary dictionary];
    }
    return _cacheModelDic;
}

@end
