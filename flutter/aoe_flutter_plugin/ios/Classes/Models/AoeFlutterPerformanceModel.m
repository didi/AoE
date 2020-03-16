//
//  AoeFlutterPerformanceModel.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import "AoeFlutterPerformanceModel.h"

@implementation AoePerformaceItem

- (NSDictionary *)jsonObj {
    return @{
        @"max":@(self.max),
        @"min":@(self.min),
        @"avg":@(self.avg)
    };
}

+ (AoePerformaceItem *)Null {
    static AoePerformaceItem * _singleton;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _singleton = [AoePerformaceItem new];
        _singleton.max = 0.0;
        _singleton.min = 0.0;
        _singleton.avg = 0.0;
    });
    return _singleton;
}

@end

@implementation AoePerformanceInfo

- (NSDictionary *)jsonObj {
    NSMutableDictionary * result = [NSMutableDictionary dictionary];
    result[@"cpu"] = self.cpuUsage.jsonObj ?: AoePerformaceItem.Null.jsonObj;
    result[@"mem"] = self.memUsage.jsonObj ?: AoePerformaceItem.Null.jsonObj;
    result[@"time"] = self.timeConsume.jsonObj ?: AoePerformaceItem.Null.jsonObj;
    result[@"device"] = AoeDeviceInfo.current.jsonObj;
    return result.copy;
}

@end
