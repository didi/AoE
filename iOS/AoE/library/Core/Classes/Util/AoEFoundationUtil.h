//
//  AoEFoundationUtil.h
//  AoE
//
//  Created by dingchao on 2019/5/11.
//

#import <Foundation/Foundation.h>

@interface AoEFoundationUtil : NSObject

+ (NSString *)aoe_MD5Data:(NSData *)data;
+ (NSString *)aoe_Base64Data:(NSData *)data;

@end
