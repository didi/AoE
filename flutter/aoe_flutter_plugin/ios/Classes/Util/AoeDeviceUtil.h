//
//  AoeDeviceUtil.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

//String name;
//String uid;
//String model;
@interface AoeDeviceUtil : NSObject
+ (NSString *)uuid;
+ (NSString *)getDeviceName;
+ (NSString *)getSysName;
+ (NSString *)getSysVersion;
+ (NSString *)getDeviceModel;
+ (NSString *)getCPUType;
+ (CGFloat)getCPUUsage;
+ (int64_t)getTotalDiskSpace;
+ (NSUInteger)useMemoryForApp;
+ (long long)totalMemoryForDevice;
+ (NSString *)getMacAddress;
+ (NSString *)getDeviceIP;
@end

NS_ASSUME_NONNULL_END
