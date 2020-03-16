//
//  AoePerformanceUtil.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import "AoePerformanceUtil.h"
#import "AoeDeviceUtil.h"

@interface AoePerformanceUtil ()
@property (nonatomic, assign)  CFTimeInterval beginTime;
@property (nonatomic, assign)  CFTimeInterval endTime;
@property (nonatomic, assign)  float cpuUsageStart;
@property (nonatomic, assign)  float cpuUsageEnd;
@property (nonatomic, assign)  float memUsageStart;
@property (nonatomic, assign)  float memUsageEnd;
@end

@implementation AoePerformanceUtil

@synthesize performanceInfo = _performanceInfo;

- (void)markStart {
    _performanceInfo = nil;
    self.beginTime = [NSDate date].timeIntervalSince1970 * 1000;
    self.cpuUsageStart = [AoeDeviceUtil getCPUUsage];
    self.memUsageStart = [AoeDeviceUtil useMemoryForApp];
}

- (void)markEnd {
    self.endTime = [NSDate date].timeIntervalSince1970*1000;
    self.cpuUsageEnd = [AoeDeviceUtil getCPUUsage];
    self.memUsageEnd = [AoeDeviceUtil useMemoryForApp];
    [self updatePreformanceInfo];
}

- (void)updatePreformanceInfo {
    _performanceInfo = [[AoePerformanceInfo alloc]init];
    //cpu
    AoePerformaceItem * cpu = [AoePerformaceItem new];
    cpu.max = MAX(self.cpuUsageStart, self.cpuUsageEnd);
    cpu.min = MIN(self.cpuUsageStart, self.cpuUsageEnd);
    cpu.avg = (cpu.max + cpu.min)/2.0;
    _performanceInfo.cpuUsage = cpu;
    //mem
    AoePerformaceItem *  mem = [AoePerformaceItem new];
    mem.max = MAX(self.memUsageStart, self.memUsageEnd);
    mem.min = MIN(self.memUsageStart, self.memUsageEnd);
    mem.avg = (mem.max + mem.min)/2.0;
    _performanceInfo.memUsage = mem;
    //time
    AoePerformaceItem *  time = [AoePerformaceItem new];
    time.max = self.endTime - self.beginTime;
    time.min = time.max;
    time.avg = (time.max + time.min)/2.0;
    _performanceInfo.timeConsume = time;
}

@end
