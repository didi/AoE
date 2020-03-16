//
//  AoeMNNComonInterceptor.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/12.
//

#import "AoeMNNComonInterceptor.h"
#import "AoeFlutterModelOption.h"
#import "AoeFlutterInputModel.h"
// #import <AoERuntime/ARMNNAdapter.h>
#import <AoE/AoElogger.h>

@interface AoeMNNComonInterceptor ()
// @property(nonatomic ,strong) ARMNNAdapter *interpreter;
@property(nonatomic ,strong) AoElogger *logger;
@end

@implementation AoeMNNComonInterceptor

- (void)close {
//    self.interpreter = nil;
}

- (BOOL)isReady {
    return YES;
//    return (self.interpreter != nil);
}

- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.logger;
}

- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input {
    
    if (![self isReady]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"TFLInterpreter instance is not ready"]];
        return nil;
    }
    
//    AoeFlutterInputModel * inputModel = (AoeFlutterInputModel *)input;
    // self.interpreter.blockSize = inputModel.blockSize;
    // self.interpreter.sourceSize = inputModel.sourceSize;
    // self.interpreter.meanVals = inputModel.meanVals.data.bytes;
    // self.interpreter.normVals = inputModel.normVals.data.bytes;
    // self.interpreter.sourceFormat = inputModel.sourceFormat;
    // self.interpreter.targetFormat = inputModel.targetFormat;
    
    NSArray *outDatas = nil;
    // if (self.interpreter &&
    //     [self.interpreter respondsToSelector:@selector(run:)]) {
    //     outDatas = [self.interpreter run:@[inputModel.data.data]];
    // }
    
    // if (!outDatas ||
    //     outDatas.count < 1) {
    //     [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outData error "]];
    //     return nil;
    // }
    return (id<AoEOutputModelProtocol>)outDatas;
    
}

- (BOOL)setupModel:(NSArray<id<AoEModelOptionProtocol>> *)options {
    if (options.count == 0) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"model config not exist 【 class AoEModelOptionProtocol】"]];
        return NO;
    }
    AoeFlutterModelOption *option = (AoeFlutterModelOption *)options.firstObject;
    NSString *fileName = option.modelFileName;
    NSString *modelPath = option.modelDirPath;
    // self.interpreter = [[ARMNNAdapter alloc] initWithPath:modelPath model:fileName];
//    return self.interpreter != nil;
    return YES;
}

#pragma getter and setter

- (AoElogger *)logger {
    if (!_logger) {
        _logger = [[AoElogger alloc] initWithTag:NSStringFromClass([self class])];
    }
    return _logger;
}

@end
