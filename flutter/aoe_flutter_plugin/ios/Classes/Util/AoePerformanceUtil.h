//
//  AoePerformanceUtil.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import <Foundation/Foundation.h>
#import "AoeFlutterPerformanceModel.h"

@interface AoePerformanceUtil : NSObject

@property (nonatomic, strong ,readonly) AoePerformanceInfo* performanceInfo;

- (void)markStart;

- (void)markEnd;

@end

