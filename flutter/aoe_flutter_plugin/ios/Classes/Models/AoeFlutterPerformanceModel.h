//
//  AoeFlutterPerformanceModel.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import <Foundation/Foundation.h>
#import "AoeDeviceInfo.h"

/// 性能项指标数据
@interface AoePerformaceItem : NSObject
@property (nonatomic, assign) float max;
@property (nonatomic, assign) float min;
@property (nonatomic, assign) float avg;
- (NSDictionary *)jsonObj;
+ (instancetype)Null;
@end

/// Aoe性能相关数据
@interface AoePerformanceInfo : NSObject

@property (nonatomic, strong) AoePerformaceItem * cpuUsage;

@property (nonatomic, strong) AoePerformaceItem * memUsage;

@property (nonatomic, strong) AoeDeviceInfo * deviceInfo;

@property (nonatomic, strong ) AoePerformaceItem * timeConsume;

- (NSDictionary *)jsonObj;

@end
