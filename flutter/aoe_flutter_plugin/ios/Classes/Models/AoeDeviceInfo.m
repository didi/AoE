//
//  AoeDeviceInfo.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import "AoeDeviceInfo.h"
#import "AoeDeviceUtil.h"

@implementation AoeDeviceInfo

- (NSDictionary *)jsonObj {
    NSMutableDictionary * result = [NSMutableDictionary dictionary];
    result[@"uuid"] = self.uuid ?: @"";
    result[@"name"] = self.name ?: @"";
    result[@"model"] = self.model ?: @"";
    result[@"system"] = self.system ?: @"";
    result[@"version"] = self.version ?: @"";
    result[@"disk"] = self.disk ?: @"";
    result[@"memory"] = self.memory ?: @"";
    result[@"ip"] = self.ip ?: @"";
    result[@"macAddress"] = self.macAddress ?: @"";
    return result.copy;
}

+ (instancetype) current {
    static AoeDeviceInfo* _instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _instance = [AoeDeviceInfo new];
        _instance.uuid = [AoeDeviceUtil uuid] ?: @"";
        _instance.name = [AoeDeviceUtil getDeviceName] ?: @"iphone";
        _instance.model = [AoeDeviceUtil getDeviceModel] ?:@"";
        _instance.system = [AoeDeviceUtil getSysName]?:@"iOS";
        _instance.version = [AoeDeviceUtil getSysVersion]?:@"";
        _instance.cpu = [AoeDeviceUtil getCPUType] ?:@"";
        _instance.disk = @([AoeDeviceUtil getTotalDiskSpace]).stringValue ?:@"";
        _instance.memory = @([AoeDeviceUtil totalMemoryForDevice]).stringValue ?:@"";
        _instance.ip = [AoeDeviceUtil getDeviceIP] ?:@"";
        _instance.macAddress = [AoeDeviceUtil getMacAddress] ?:@"";
    });
    return _instance;
}

@end
