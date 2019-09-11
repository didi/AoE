//
//  AoEUpgradeService.h
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import <Foundation/Foundation.h>
#import "AoEUpgradeServiceInputModel.h"

@interface AoEUpgradeService : NSObject

+ (AoEUpgradeService *)shareInstance;

- (void)startUpgradeService:(AoEUpgradeServiceInputModel *)model;

@end
