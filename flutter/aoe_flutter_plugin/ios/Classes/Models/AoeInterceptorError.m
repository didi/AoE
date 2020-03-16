//
//  AoeInterceptorError.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/12.
//

#import "AoeInterceptorError.h"

@implementation AoeInterceptorError

+ (AoeInterceptorError * )parameterError {
    return [self errowWithType:AoeInValidParameter msg:@"the parameters is invalid"];
}

+ (AoeInterceptorError * )interceptorError {
    return [self errowWithType:AoeInValidInterceptorError msg:@"can not suppport the infer inframework or can not load the model"];
}

+ (AoeInterceptorError * )ModelError {
    return [self errowWithType:AoeInValidModelError msg:@"model does not exist "];
}

+ (AoeInterceptorError * )sampleError {
    return [self errowWithType:AoeInValidSampleError msg:@"sample data invalid"];
}

+ (instancetype)errowWithType:(AoeInterceptorErrorType)type msg:(NSString *)msg {
    return [[self alloc]initWithType:type msg:msg];
}

- (instancetype)initWithType:(AoeInterceptorErrorType)type msg:(NSString *)msg {
    if (self = [super init]) {
        self.errType = type;
        self.errMsg = msg;
    }
    return self;
}

- (NSDictionary *)dictionary {
    NSMutableDictionary * dict = [NSMutableDictionary dictionary];
    [dict setObject:@(self.errType) forKey:@"errType"];
    [dict setObject:self.errMsg?:@"" forKey:@"errMsg"];
    return dict.copy;
}

@end
