//
//  AoEDeviceInfoUtil.h
//  AoE
//
//  Created by dingchao on 2019/5/21.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, AEDevicePerformanceType) {
    AEDevicePerformanceTypeForLow = 0,
    AEDevicePerformanceTypeForMid,
    AEDevicePerformanceTypeForHigh,
};

@interface AoEDeviceInfoUtil : NSObject

+ (AEDevicePerformanceType)devicePerformanceType;

@end

