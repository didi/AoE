//
//  AoEUpgradeModel.h
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import <Foundation/Foundation.h>

@interface AoEUpgradeModel : NSObject

/** 模型名称 */
@property (nonatomic, copy) NSString *name;

/** 更新进行中 */
@property (nonatomic, assign) BOOL upgrading;

/** 上次更新时间 */
@property (nonatomic, strong) NSDate *lastUpgradeDate;


@end
