//
//  AoeFlutterOutputModel.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/30.
//

#import "AoeFlutterOutputModel.h"

@implementation AoeFlutterOutputModel

+ (instancetype)outputModelWithError:(AoeInterceptorError *)error {
    AoeFlutterOutputModel * output = [AoeFlutterOutputModel new];
    output.error = error;
    return output;
}

+ (instancetype)outputModelWithData:(id)data performance:(AoePerformanceInfo *)performance {
    AoeFlutterOutputModel * output = [AoeFlutterOutputModel new];
    output.data = data;
    output.performance = performance;
    return output;
}

- (NSDictionary *)jsonObj {
    NSMutableDictionary * _jsonObj = [NSMutableDictionary dictionary];
    _jsonObj[@"data"] = self.data ?: @[];
    _jsonObj[@"error"] = self.error.dictionary ?: @{};
    _jsonObj[@"performance"] = self.performance.jsonObj ?: @{};
    return _jsonObj.copy;
}

@end
