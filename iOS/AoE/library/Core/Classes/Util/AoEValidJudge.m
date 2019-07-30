//
//  AoEValidJudge.m
//  AoE
//
//  Created by dingchao on 2019/3/21.
//

#import "AoEValidJudge.h"

@implementation AoEValidJudge

/*
 是否是有效字符串
 */
+ (BOOL)isValidString:(NSString *)aString {
    if ([aString isKindOfClass:[NSString class]] &&
        aString.length > 0) {
        return YES;
    }
    return NO;
}

/*
 是否是有效NSArray
 */
+ (BOOL)isValidArray:(NSArray *)aArray {
    if ([aArray isKindOfClass:[NSArray class]]) {
        return YES;
    }
    return NO;
}

/*
 是否是有效NSDictionary
 */
+ (BOOL)isValidDictionary:(NSDictionary *)aDictionary {
    if ([aDictionary isKindOfClass:[NSDictionary class]]) {
        return YES;
    }
    return NO;
}

@end
