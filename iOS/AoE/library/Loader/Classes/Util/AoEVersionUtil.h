//
//  AoEVersionUtil.h
//  AoE
//
//  Created by dingchao on 2019/3/26.
//

#import <Foundation/Foundation.h>

@interface AoEVersionUtil : NSObject
/** 是否是合法格式版本 */
+ (BOOL)isValidVersion:(NSString *)version;
/** versionA比versionB版本大（注意:版本为非数字格式，返回NO） */
+ (BOOL)version:(NSString *)versionA isGreaterThanVersion:(NSString *)versionB;
/** version比versionArray版本大（注意:版本为非数字格式，返回NO） */
+ (BOOL)version:(NSString *)version isGreaterThanVersions:(NSArray *)versionArray;
/** 找出最新版本 */
+ (NSString *)findLastVersionFromVersions:(NSArray *)versions;

@end

