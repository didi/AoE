//
//  AoEUpgradeServiceInputModel.m
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import "AoEUpgradeServiceInputModel.h"
#import "AoEValidJudge.h"
#import "AoECryptoUtil.h"

@implementation AoEUpgradeServiceInputModel

+ (instancetype)initWithName:(NSString *)name
                     version:(NSString *)version
                         url:(NSString *)url
                 storagePath:(NSString *)path {
    AoEUpgradeServiceInputModel *model = [[AoEUpgradeServiceInputModel alloc] init];
    NSArray <NSString *> *components = [name componentsSeparatedByString:@"_"];
    model.needDownloadImmediately = NO;
    model.name = name;
    model.alias = components.count > 1 ? components.lastObject : name;
    model.version = version;
    model.url = url;
    model.storagePath = path;
    return model;
}

+ (instancetype)initWithModuleOption:(AoEModelOption *)option appKey:(NSString *)appKey storagePath:(NSString *)path {
    NSString *url = [self getURLWithOption:option appKey:appKey];
    return [AoEUpgradeServiceInputModel initWithName:[option.tag stringByAppendingFormat:@"_%@",option.modelDir] version:option.version url:url storagePath:path];
}

+ (NSDictionary *)requestParamsWithOption:(AoEModelOption *)option appKey:(NSInteger)appKey {
    if (appKey > 0) {
        return nil;
    }
    
    NSMutableDictionary *requestParams = [NSMutableDictionary dictionaryWithDictionary:@{@"appId":@(appKey),
             @"modelId":@(option.modelId),
             @"modelVersionCode":option.version,
             @"timeStamp":@([NSDate date].timeIntervalSince1970),
             @"deviceSN":@"",
                                                                                         @"deviceType":@"", @"kLat":@(39.92),
                                                                                         @"kLng":@(116.46),
    }];
    
    NSString *sgin = [AoECryptoUtil aoe_encryptAoEReqParams:requestParams.copy encryptKey:@(appKey).stringValue];
    [requestParams addEntriesFromDictionary:@{@"sign":sgin}];
    return requestParams.copy;
    
    // kLat kLng sign
}

+ (NSString *)getURLWithOption:(AoEModelOption *)option appKey:(NSString *)appKey {
    NSString *host = @"https://aoe-test.xiaojukeji.com";
    NSString *method = @"upgradeModel";
    return [host stringByAppendingPathComponent:method];
}

@end
