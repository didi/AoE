//
//  AoEVersionUtil.m
//  AoE
//
//  Created by dingchao on 2019/3/26.
//

#import "AoEVersionUtil.h"
#import <Foundation/NSScanner.h>

@implementation AoEVersionUtil

+ (BOOL)isValidVersion:(NSString *)version {
    return version.integerValue > 0;
}

+ (BOOL)version:(NSString *)versionA isGreaterThanVersion:(NSString *)versionB {
    if (![AoEVersionUtil isValidVersion:versionA]) {
        return NO;
    }
    if (![AoEVersionUtil isValidVersion:versionB]) {
        return YES;
    }
    return versionA.integerValue > versionB.integerValue;
}

+ (BOOL)version:(NSString *)version isGreaterThanVersions:(NSArray *)versionArray {
    if (![AoEVersionUtil isValidVersion:version]) {
        return NO;
    }
    
    for (NSString *subVersion in versionArray) {
        if (![AoEVersionUtil isValidVersion:subVersion]) {
            return YES;
        }
        
        if (![AoEVersionUtil version:version isGreaterThanVersion:subVersion]) {
            return NO;
        }
    }

    return YES;
}

+ (NSString *)findLastVersionFromVersions:(NSArray *)versions {
    if (versions.count == 0) {
        return nil;
    }
    
    if (versions.count == 1) {
        return versions.firstObject;
    }
    
    NSString *lastVersion = versions.firstObject;
    for (NSUInteger index = 1; index < versions.count; index++) {
        NSString *version = versions[index];
        if ([AoEVersionUtil version:version isGreaterThanVersion:lastVersion]) {
            lastVersion = version;
        }
    }
    
    return lastVersion;
}

@end
