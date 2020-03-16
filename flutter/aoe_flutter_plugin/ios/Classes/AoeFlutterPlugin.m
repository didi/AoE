#import "AoeFlutterPlugin.h"
#import <AoE/AoEClient.h>
#import <AoE/AoEClientOption.h>
#import <AoE/AoEModelOption.h>
#import <AoE/AoEModelManager.h>
#import "AoeFlutterProcessor.h"
#import "AoeCommonInterceptor.h"
#import "AoeInterceptorError.h"
#import "AoeFlutterInputModel.h"
#import "AoePerformanceUtil.h"
#import "AoeFlutterOutputModel.h"

@interface AoeFlutterPlugin ()

@property (nonatomic, strong) AoePerformanceUtil * performanceUtil;

@property (nonatomic, strong) AoEClient * client;

@property (nonatomic, assign) AoeInterceptorType runtimeType;

@end

@implementation AoeFlutterPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"aoe_flutter"
                                     binaryMessenger:[registrar messenger]];
    
    AoeFlutterPlugin* instance = [[AoeFlutterPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
    
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"start" isEqualToString:call.method]) {
        [self startByMethodCall:call result:result];
    } else if([@"process" isEqualToString:call.method]){
        [self processByMethodCall:call result:result];
    }else if([@"stop" isEqualToString:call.method]){
        [self stopByMethodCall:call result:result];
    }else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - AOE 处理流程

// 初始化
- (void)startByMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary * inputs = (NSDictionary *) call.arguments;
    if (!inputs || ![inputs isKindOfClass:[NSDictionary class]]) {
        result([AoeFlutterOutputModel outputModelWithError:[AoeInterceptorError parameterError]].jsonObj);
        return;
    }
    
    AoeInterceptorType interceptorType = inputs[@"runtime"] ? [inputs[@"runtime"] integerValue] : AoeInterceptorTypeUnkonw;
    if (interceptorType == AoeInterceptorTypeUnkonw) {
        result([AoeFlutterOutputModel outputModelWithError:[AoeInterceptorError interceptorError]].jsonObj);
        return;
    }
    self.runtimeType = interceptorType;
    
    NSString * modelDir = inputs[@"modelDir"];
    if (!modelDir) {
        result([AoeFlutterOutputModel outputModelWithError:[AoeInterceptorError ModelError]].jsonObj);
        return;
    }
    
    AoEClientOption * option = [AoEClientOption new];
    option.processorClassName = @"AoeFlutterProcessor";
    option.interpreterClassName = @"AoeCommonInterceptor";
    option.modelOptionLoaderClassName = @"AoeFlutterModelManager";
    option.runtimeType = self.runtimeType;
    
    AoEClient * client = [[AoEClient alloc]initWithClientOption:option modelDir:modelDir subDir:nil];
    [client setupModel:^(AoEClientStatusCode statusCode) {
        if (statusCode != AoEClientStatusCodeForModelLoadAlready) {
            AoeInterceptorError * error = [[AoeInterceptorError alloc] initWithType:AoeInValidModelError msg:@"load model error ，please check model configuration"];
            result([AoeFlutterOutputModel outputModelWithError:error].jsonObj);
        }else {
            result([AoeFlutterOutputModel outputModelWithData:@(YES) performance:nil].jsonObj);
        }
    }];
    self.client = client;
}

// 处理中
- (void)processByMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary * inputs = (NSDictionary *) call.arguments;
    if (!inputs || ![inputs isKindOfClass:[NSDictionary class]]) {
        result([AoeFlutterOutputModel outputModelWithError:[AoeInterceptorError parameterError]].jsonObj);
        return;
    }
    
    AoeFlutterInputModel * input = [[AoeFlutterInputModel alloc]initWithDict:call.arguments];
    if (![input isValid]) {
        result([AoeFlutterOutputModel outputModelWithError:[AoeInterceptorError parameterError]].jsonObj);
        return;
    }
    [self.performanceUtil markStart];
    NSArray<AoEOutputModelProtocol> * output = (NSArray<AoEOutputModelProtocol> *)[self.client process:input];
    if (!output.count) {
        AoeInterceptorError * error = [[AoeInterceptorError alloc] initWithType:AoeProcessError msg:@"infer failed"];
        result([AoeFlutterOutputModel outputModelWithError:error].jsonObj);
        return;
    }
    //tensor flow返回数据格式为 [[NSData],[],....] 需要把NSData转换为vFlutterStandardTypedData
    NSArray * finalList = [NSArray array];
    if (self.runtimeType == AoeInterceptorTypeTFlow) {//返回数据格式为[[NSData],[],....] NSData需要转换为FlutterStandardTypedData
        NSMutableArray * list = [NSMutableArray array];
        for (id item in output) {
            if (![item isKindOfClass:[NSData class]]) {
                continue;
            }
            FlutterStandardTypedData * flutterData = [FlutterStandardTypedData typedDataWithBytes:item];
            [list addObject:flutterData];
        }
        finalList = list;
    }else {
        finalList = (NSArray *) output;
    }
    [self.performanceUtil markEnd];
    result([AoeFlutterOutputModel outputModelWithData:finalList performance:self.performanceUtil.performanceInfo].jsonObj);
}

// 结束
- (void)stopByMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    [self.client close];
    result(@(YES));
}

#pragma mark - Getter

- (AoePerformanceUtil *)performanceUtil {
    if (!_performanceUtil) {
        _performanceUtil = [AoePerformanceUtil new];
    }
    return _performanceUtil;
}

@end
