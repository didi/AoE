//
//  AoeDeviceInfo.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import <Foundation/Foundation.h>

/// 设备基本信息
@interface AoeDeviceInfo :NSObject
@property (nonatomic, copy) NSString * uuid;
@property (nonatomic, copy) NSString * name;
@property (nonatomic, copy) NSString * model;
@property (nonatomic, copy) NSString * system;
@property (nonatomic, copy) NSString * version;
@property (nonatomic, copy) NSString * cpu;
@property (nonatomic, copy) NSString * disk;
@property (nonatomic, copy) NSString * memory;
@property (nonatomic, copy) NSString * ip;
@property (nonatomic, copy) NSString * macAddress;
+ (instancetype)current;
- (NSDictionary *)jsonObj;
@end;

