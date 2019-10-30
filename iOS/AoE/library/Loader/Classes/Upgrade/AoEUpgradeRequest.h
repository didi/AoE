//
//  AoEUpgradeRequest.h
//  AFNetworking
//
//  Created by dingchao on 2019/9/10.
//

#import <Foundation/Foundation.h>

typedef void (^AoEUpgradeReqSucceedBlock)(id model);
typedef void (^AoEUpgradeReqFailureBlock)(NSError *error);

@interface AoEUpgradeRequest : NSObject

+ (void)requestUpgradePlistWithUrl:(NSString *)urlString
                            params:(NSDictionary *)params
                      successBlock:(AoEUpgradeReqSucceedBlock)successBlock
                      failureBlock:(AoEUpgradeReqFailureBlock)failureBlock;

+ (void)downloadWithUrlString:(NSString *)urlString
                 successBlock:(void(^)(NSString *cachePath))successBlock
                 failureBlock:(void(^)(NSError *error))failureBlock;

@end
