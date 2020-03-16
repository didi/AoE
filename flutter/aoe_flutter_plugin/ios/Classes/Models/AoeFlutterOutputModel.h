//
//  AoeFlutterOutputModel.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import <Foundation/Foundation.h>
#import "AoeInterceptorError.h"
#import "AoeFlutterPerformanceModel.h"

@interface AoeFlutterOutputModel : NSObject

@property (nonatomic, strong) id data;

@property (nonatomic, strong) AoePerformanceInfo * performance;

@property (nonatomic, strong) AoeInterceptorError * error;

+ (instancetype)outputModelWithError:(AoeInterceptorError *)error;

+ (instancetype)outputModelWithData:(id)data performance:(AoePerformanceInfo *)performance ;

- (NSDictionary *)jsonObj;

@end
