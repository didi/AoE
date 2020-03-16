//
//  AoeInterceptorError.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/12.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, AoeInterceptorErrorType) {
    AoeInValidParameter,//参数异常
    AoeInValidInterceptorError,//推理框架无效
    AoeInValidModelError,//模型无效
    AoeInValidSampleError,//测试数据集无效
    AoeProcessError,//推理异常
};

@interface AoeInterceptorError : NSObject

@property (nonatomic, assign) AoeInterceptorErrorType errType;

@property (nonatomic, copy) NSString * errMsg;

+ (instancetype)errowWithType:(AoeInterceptorErrorType)type msg:(NSString *)msg;

- (instancetype)initWithType:(AoeInterceptorErrorType)type msg:(NSString *)msg;

+ (AoeInterceptorError * )parameterError;

+ (AoeInterceptorError * )interceptorError;

+ (AoeInterceptorError * )ModelError;

+ (AoeInterceptorError * )sampleError;

- (NSDictionary *)dictionary;//用于Flutter数据转换

@end
