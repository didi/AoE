//
//  AoEComponentProvider.m
//  AoE
//
//  Created by dingchao on 2019/7/2.
//

#import "AoEComponentProvider.h"
#import "AoEValidJudge.h"
#if __has_include("AoEModelManager.h")
#import "AoEModelManager.h"
#endif

#if __has_include("AoElogger.h")
#import "AoElogger.h"
#endif

typedef NS_ENUM(NSUInteger, AEComponentType) {
    AEComponentTypeForModelLoader,
    AEComponentTypeForInterpreter,
    AEComponentTypeForLogger,
};

static id<AoELoggerComponentProtocol> __logger = nil;
static NSMutableDictionary<NSString * ,id<AoEModelLoaderComponentProtocol>> *__modelLoaderComponentCache = nil;
static NSMutableDictionary<NSString * ,id<AoEInterpreterComponentProtocol>> *__interpreterComponentCache = nil;
static NSMutableDictionary<NSString * ,id<AoEComponentProtocol>> *__componentInstanceCache = nil;
static NSString *AoEComponentProviderLockObj = @"AoEComponentProviderLockObj";

@implementation AoEComponentProvider


+ (id<AoEComponentProtocol>)instanceComponent:(NSString *)className
                                         type:(AEComponentType)type {
    id<AoEComponentProtocol> componentInstance = nil;
    NSMutableDictionary *componentCache = nil;
    switch (type) {
        case AEComponentTypeForInterpreter:
            if (!__interpreterComponentCache) {
                __interpreterComponentCache = [NSMutableDictionary dictionaryWithCapacity:0];
            }
            componentCache = __interpreterComponentCache;
            break;
        case AEComponentTypeForModelLoader:
            if (!__modelLoaderComponentCache) {
                __modelLoaderComponentCache = [NSMutableDictionary dictionaryWithCapacity:0];
            }
            componentCache = __modelLoaderComponentCache;
            break;
        case AEComponentTypeForLogger:
            if (!__componentInstanceCache) {
                __componentInstanceCache = [NSMutableDictionary dictionaryWithCapacity:0];
            }
            componentCache = __componentInstanceCache;
            break;
        default:
            break;
    }
    
    componentInstance = componentCache[className];
    if (!componentInstance &&
        [AoEValidJudge isValidString:className] &&
        NSClassFromString(className)) {
        componentInstance = [NSClassFromString(className) new];
        if (componentInstance) {
            [componentCache addEntriesFromDictionary:@{className:componentInstance}];
        }
    }
    return componentInstance;
}

+ (id<AoEInterpreterComponentProtocol>)getInterpreter:(NSString *)className {
    if (![AoEValidJudge isValidString:className]) {
        return nil;
    }
    id<AoEInterpreterComponentProtocol> instacne = nil;
    @synchronized (AoEComponentProviderLockObj) {
        instacne = (id<AoEInterpreterComponentProtocol>)[self instanceComponent:className type:AEComponentTypeForInterpreter];
    }
    if (![instacne conformsToProtocol:@protocol(AoEInterpreterComponentProtocol)]) {
        instacne = nil;
    }
    return instacne;
}

+ (id<AoEModelLoaderComponentProtocol>)getModelLoader:(NSString *)className {
    id<AoEModelLoaderComponentProtocol> modelLoader = nil;
    @synchronized (AoEComponentProviderLockObj) {
        modelLoader = (id<AoEModelLoaderComponentProtocol>)[self instanceComponent:className type:AEComponentTypeForModelLoader];
    }
    if (modelLoader == nil ||
        ![modelLoader conformsToProtocol:@protocol(AoEModelLoaderComponentProtocol)]) {
#if __has_include("AoEModelManager.h")
         modelLoader = [AoEModelManager new];
#endif
    }
    return modelLoader;
}

+ (id<AoELoggerComponentProtocol>)getLoggerInstance:(NSString *)className {
    id<AoELoggerComponentProtocol> instacne = nil;
    @synchronized (AoEComponentProviderLockObj) {
        instacne = (id<AoELoggerComponentProtocol>)[self instanceComponent:className type:AEComponentTypeForLogger];
    }
    
    if (![instacne conformsToProtocol:@protocol(AoELoggerComponentProtocol)]) {
        instacne = nil;
    }
    return instacne;
}

+ (id<AoELoggerComponentProtocol>)logger {
    return __logger;
}

+ (void)setLogger:(id<AoELoggerComponentProtocol>)logger {
    @synchronized (AoEComponentProviderLockObj) {
        __logger = logger;
    }
}

@end
