//
//  AoEUpgradeServiceInputModel.m
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import "AoEUpgradeServiceInputModel.h"

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

@end
