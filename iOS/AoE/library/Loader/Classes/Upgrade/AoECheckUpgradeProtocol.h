//
//  AoECheckUpgradeProtocol.h
//  AoE
//
//  Created by dingchao on 2019/9/11.
//

#import <Foundation/Foundation.h>

@protocol AoECheckUpgradeProtocol <NSObject>

+ (instancetype)instanceWithDictionary:(NSDictionary *)dictary;
- (NSString *)version;
- (NSString *)downloadUrl;
- (NSNumber *)size;
- (NSString *)sign;

@end
