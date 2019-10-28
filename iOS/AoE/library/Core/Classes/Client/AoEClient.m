//
//  AoEClient.m
//  AoE
//
//  Created by dingchao on 2019/3/27.
//

#import "AoEClient.h"
#import "AoEProcessor.h"
#import "AoEClientOption.h"
#import "AoEComponentProvider.h"
#import "AoEValidJudge.h"
#import "AoEMacrosDefault.h"
#if __has_include("AoElogger.h")
#import "AoElogger.h"
#endif
@interface AoEClient ()
@property (nonatomic ,strong) NSMutableArray <id<AoEModelOptionProtocol>> *options;
@property (nonatomic ,strong) id<AoEProcessorProtocol> processor;
@property (nonatomic ,strong) AoEClientOption *clientOption;
@property (nonatomic ,strong) AoEClientReadyBlock reeadyBlock;
@property (nonatomic ,assign) AoEClientStatusCode statusCode;
@property (nonatomic ,assign) BOOL running;
@property (nonatomic ,strong) id<AoELoggerComponentProtocol> logger;
@end

@implementation AoEClient

- (instancetype)initWithClientOption:(AoEClientOption *)clientOption
                            modelDir:(NSString *)mainModelDir
                              subDir:(NSArray <NSString *>*)subsequentModelDirs {
    if (self = [super init]) {
        _clientOption = clientOption;
        [self setupLoggerFromClass:clientOption.loggerClassName];
        [self.logger debugLog:[NSString stringWithFormat:@"AoEClientOption after statusCode: %@",self.clientOption]];
        [self setupProcessorFromClass:clientOption.processorClassName];
        
        id<AoEModelLoaderComponentProtocol> modelLoader = [self modelLoaderComponentInstance];
        self.statusCode = [self tryLoadModelConfig:modelLoader mainDir:mainModelDir subDir:subsequentModelDirs];
        [self.logger debugLog:[NSString stringWithFormat:@"LoadModelConfig after statusCode: %@",@(self.statusCode)]];
    }
    return self;
}

- (void)setupModel:(AoEClientReadyBlock)readyBlock {
    [self setupModelImmediately:YES success:readyBlock];
}

- (void)setupModelImmediately:(BOOL)immediately success:(AoEClientReadyBlock)readyBlock {
    self.reeadyBlock =  readyBlock;
    if (immediately) {
        [self setupModelInternal:readyBlock];
    }
}

- (id<AoEOutputModelProtocol>)process:(id<AoEInputModelProtocol>)input {
    if (![self isModelReady]) {
        [self setupModelInternal:self.reeadyBlock];
    }
    if (!self.processor) {
        self.processor = [AoEProcessor new];
    }
    [self.logger infoLog:@"process begin"];
    [self.logger debugLog:[NSString stringWithFormat:@"input data: %@",input]];
    SafeBlockRun(self.beforeProcessBlock, self ,input);
    id<AoEOutputModelProtocol> output = nil;
    id<AoEInterpreterComponentProtocol> interpreter = [self interpreterComponentInstance];
    if ([interpreter respondsToSelector:@selector(run:)]) {
        output = [interpreter run:input];
    }
    SafeBlockRun(self.afterProcessBlock, self ,output);
    
    [self.logger debugLog:[NSString stringWithFormat:@"output data: %@",output]];
    [self.logger infoLog:@"process finish"];
    return output;
}

- (void)close {
    [self.logger infoLog:@"interpreter will close"];
    id<AoEInterpreterComponentProtocol> interpreter = [self interpreterComponentInstance];
    if ([interpreter respondsToSelector:@selector(close)]) {
      [interpreter close];
    }
    [self.logger infoLog:@"interpreter did closed"];
    self.processor = nil;
}

- (void)dealloc {
    [self close];
}

#pragma mark - private method

- (void)setupLoggerFromClass:(NSString *)loggerClassName {
    if ([AoEValidJudge isValidString:loggerClassName] && NSClassFromString(loggerClassName) &&
        [NSClassFromString(loggerClassName) instancesRespondToSelector:@selector(setLogTag:)]) {
        self.logger = [NSClassFromString(loggerClassName) new];
        [self.logger setLogTag:@"Client"];
    }else {
#if __has_include("AoElogger.h")
        self.logger = [[AoElogger alloc] initWithTag:@"Client"];
#endif
    }
}

- (void)setupProcessorFromClass:(NSString *)processorClassName {
    if ([AoEValidJudge isValidString:processorClassName] && NSClassFromString(processorClassName) &&
        [NSClassFromString(processorClassName) instancesRespondToSelector:@selector(initWithClientOption:)]) {
        self.processor = [[NSClassFromString(processorClassName) alloc] initWithClientOption:self.clientOption];
    }else {
        self.processor = [[AoEProcessor alloc] initWithClientOption:self.clientOption];
    }
}

- (id<AoEModelLoaderComponentProtocol>)modelLoaderComponentInstance {
    id<AoEModelLoaderComponentProtocol> modelLoader = nil;
    if (self.processor &&
        [self.processor respondsToSelector:@selector(getModelLoaderComponent)]) {
        modelLoader = [self.processor getModelLoaderComponent];
    }
    return modelLoader;
}

- (id<AoEInterpreterComponentProtocol>)interpreterComponentInstance {
    id<AoEInterpreterComponentProtocol> interpreter = nil;
    if (self.processor &&
        [self.processor respondsToSelector:@selector(getInterpreterComponent)]) {
        interpreter = [self.processor getInterpreterComponent];
    }
    return interpreter;
}

- (AoEClientStatusCode)tryLoadModelConfig:(id<AoEModelLoaderComponentProtocol>)modelLoader
                                  mainDir:(NSString *)mainModelDir
                                   subDir:(NSArray <NSString *>*)subsequentModelDirs {
    
    if (!modelLoader ||
        ![modelLoader conformsToProtocol:@protocol(AoEModelLoaderComponentProtocol)]) {
        [self.logger errorLog:[NSString stringWithFormat:@"modelLoader did not conformsToProtocol %@",modelLoader]];
        return AoEClientStatusCodeForLoaderNotExist;
    }
    
    if (![AoEValidJudge isValidArray:self.options]) {
        self.options = [NSMutableArray arrayWithCapacity:0];
    }
    NSMutableArray <id<AoEModelOptionProtocol>> *array = [NSMutableArray arrayWithCapacity:0];
    // 获取模型 支持有多个模型的情况。
    id<AoEModelOptionProtocol> option = [self loader:modelLoader modelOptionWithPath:mainModelDir];
    if ([option respondsToSelector:@selector(isValidOption)] &&
        [option isValidOption]) {
        [self.logger infoLog:[NSString stringWithFormat:@"success added model from %@",mainModelDir]];
        [array addObject:option];
        if ([AoEValidJudge isValidArray:subsequentModelDirs]) {
            for (NSString *subModelDir in subsequentModelDirs) {
                id<AoEModelOptionProtocol> subOption = [self loader:modelLoader modelOptionWithPath:subModelDir];
                if ([option respondsToSelector:@selector(isValidOption)] &&
                    [option isValidOption]) {
                    [self.logger infoLog:[NSString stringWithFormat:@"success added model from %@",subModelDir]];
                    [array addObject:subOption];
                }else {
                    [self.logger errorLog:[NSString stringWithFormat:@"add model from %@ fail ",subModelDir]];
                    return AoEClientStatusCodeForConfigParseError;
                }
            }
        }
    }else {
        [self.logger errorLog:[NSString stringWithFormat:@"add model from %@ fail ",mainModelDir]];
        return AoEClientStatusCodeForConfigParseError;
    }
    [self.options removeAllObjects];
    [self.options addObjectsFromArray:array];
    return AoEClientStatusCodeForModelConfigReady;
}

- (void)setupModelInternal:(AoEClientReadyBlock)readyBlock {
    if ([self isModelOptionReady]) {
        id<AoEInterpreterComponentProtocol> interpreter = [self interpreterComponentInstance];
        BOOL setupReady = NO;
        if ([interpreter respondsToSelector:@selector(setupModel:)]) {
             setupReady = [interpreter setupModel:self.options];
        }
        if (setupReady) {
            self.statusCode = AoEClientStatusCodeForModelLoadAlready;
        } else {
            self.statusCode = AoEClientStatusCodeForModelLoadError;
        }
    }
    [self.logger debugLog:[NSString stringWithFormat:@"setup model after %@",@(self.statusCode)]];
    SafeBlockRun(readyBlock, self.statusCode);
}

- (id<AoEModelOptionProtocol>)loader:(id<AoEModelLoaderComponentProtocol>)modelLoader
                 modelOptionWithPath:(NSString *)path {
    id<AoEModelOptionProtocol> option = nil;
    if ([modelLoader respondsToSelector:@selector(loadModelConfig:ext:)]) {
        option = [modelLoader loadModelConfig:path ext:@{
            @"appId":@(self.clientOption.appId),
            @"lat":self.clientOption.lat,
            @"lng":self.clientOption.lng
        }];
    }
    return option;
}

- (BOOL)isModelOptionReady {
    return self.statusCode >= AoEClientStatusCodeForModelConfigReady && self.options.count > 0;
}

- (BOOL)isModelReady {
    return self.statusCode >= AoEClientStatusCodeForModelLoadAlready;
}

@end


