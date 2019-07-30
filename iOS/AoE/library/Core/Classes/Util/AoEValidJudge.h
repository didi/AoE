//
//  AoEValidJudge.h
//  AoE
//
//  Created by dingchao on 2019/3/21.
//

#import <Foundation/Foundation.h>

@interface AoEValidJudge : NSObject

/*
 是否是有效字符串
 */
+ (BOOL)isValidString:(NSString *)aString;

/*
 是否是有效NSArray
 */
+ (BOOL)isValidArray:(NSArray *)aArray;

/*
 是否是有效NSDictionary
 */
+ (BOOL)isValidDictionary:(NSDictionary *)aDictionary;

@end

