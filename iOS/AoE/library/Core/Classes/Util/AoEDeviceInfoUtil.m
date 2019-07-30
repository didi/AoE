//
//  AoEDeviceInfoUtil.m
//  AoE
//
//  Created by dingchao on 2019/5/21.
//

#import "AoEDeviceInfoUtil.h"
#import <sys/utsname.h>

@implementation AoEDeviceInfoUtil

+ (AEDevicePerformanceType)devicePerformanceType {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *platform = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    
    if ([self lowPerformanceDevice:platform]){
        return AEDevicePerformanceTypeForLow;
    }else if([self midPerformanceDevice:platform]){
         return AEDevicePerformanceTypeForMid;
    }else if([self heighPerformanceDevice:platform]){
        return AEDevicePerformanceTypeForHigh;
    }
    return AEDevicePerformanceTypeForLow;
}

+ (BOOL)lowPerformanceDevice:(NSString *)platform {
    if ([platform isEqualToString:@"iPhone1,1"] ||       //@"iPhone 1G"
        [platform isEqualToString:@"iPhone1,2"] ||       //@"iPhone 3G";
        [platform isEqualToString:@"iPhone2,1"] ||       //@"iPhone 3GS";
        [platform isEqualToString:@"iPhone3,1"] ||       //@"iPhone 4";
        [platform isEqualToString:@"iPhone3,2"] ||       //@"iPhone 4";
        [platform isEqualToString:@"iPhone4,1"] ||       //@"iPhone 4S";
        [platform isEqualToString:@"iPhone5,1"] ||       //@"iPhone 5";
        [platform isEqualToString:@"iPhone5,2"] ||       //@"iPhone 5";
        [platform isEqualToString:@"iPhone5,3"] ||       //@"iPhone 5C";
        [platform isEqualToString:@"iPhone5,4"] ||       //@"iPhone 5C";
        [platform isEqualToString:@"iPhone6,1"] ||       //@"iPhone 5S";
        [platform isEqualToString:@"iPhone6,2"] ||       //@"iPhone 5S";
        [platform isEqualToString:@"iPhone7,1"] ||       //@"iPhone 6 Plus";
        [platform isEqualToString:@"iPhone7,2"] ){       //@"iPhone 6";
        return YES;
    }
    return NO;
}

+ (BOOL)heighPerformanceDevice:(NSString *)platform {
    if([platform isEqualToString:@"iPhone10,1"]|| //@"iPhone 8";
       [platform isEqualToString:@"iPhone10.4"]|| //@"iPhone 8";
       [platform isEqualToString:@"iPhone10,2"]|| //@"iPhone 8 Plus";
       [platform isEqualToString:@"iPhone10,5"]|| //@"iPhone 8 Plus";
       [platform isEqualToString:@"iPhone10,3"]|| //@"iPhone X";
       [platform isEqualToString:@"iPhone10,6"]|| //@"iPhone X";
       [platform isEqualToString:@"iPhone11,8"]|| //@"iPhone XR";
       [platform isEqualToString:@"iPhone11,2"]|| //@"iPhone XS";
       [platform isEqualToString:@"iPhone11,4"]|| //@"iPhone XS MAX";
       [platform isEqualToString:@"iPhone11,6"]){ //@"iPhone XS MAX";
        return YES;
    }
    return NO;
}

+ (BOOL)midPerformanceDevice:(NSString *)platform {
    if([platform isEqualToString:@"iPhone8,2"] || //@"iPhone 6S Plus";
       [platform isEqualToString:@"iPhone8,1"] || //@"iPhone 6S";
       [platform isEqualToString:@"iPhone8,4"] || //@"iPhone SE"
       [platform isEqualToString:@"iPhone9,1"] || //@"iPhone 7";
       [platform isEqualToString:@"iPhone9,3"] || //@"iPhone 7";
       [platform isEqualToString:@"iPhone9,2"] || //@"iPhone 7 Plus";
       [platform isEqualToString:@"iPhone9,4"]){  //@"iPhone 7 Plus";
        return YES;
    }
    return NO;
}

@end
