//
//  ABTensorFlowAdapter.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/22.
//

#import "ABTensorFlowAdapter.h"
#import <AoE/AoElogger.h>
#import <TensorFlowLiteObjC/TFLInterpreter.h>
#import <TensorFlowLiteObjC/TFLTensor.h>

@interface ABTensorFlowAdapter ()
@property(nonatomic ,strong) TFLInterpreter *interpreter;
@property(nonatomic ,strong) AoElogger *logger;
@end

@implementation ABTensorFlowAdapter

- (instancetype)initWithPath:(NSString *)path {
    self = [super init];
    NSError *error = nil;
    if (self) {
        self.interpreter = [[TFLInterpreter alloc] initWithModelPath:path error:&error];
        if (error) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"TFLInterpreter init \
                                              error message %@",
                                              error.localizedDescription]];
            return nil;
        }
    }
    return self;
}

- (void)dealloc {
    [self close];
}

- (void)close {
    self.interpreter = nil;
}

- (BOOL)isReady {
    return (self.interpreter != nil);
}

- (NSData *)run:(NSData *)inputData {
    
    if (!inputData ||
        ![inputData isKindOfClass:[NSData class]]) {
        return nil;
    }
    
    // 使用过程参照 https://github.com/tensorflow/tensorflow/tree/master/tensorflow/lite/experimental/objc
    // 初始化 TensorFlow 所有 Tensor
    if (![self allocateTensors:self.interpreter] ||
        ![self setupInputData:inputData interpreter:self.interpreter] ||
        ![self invokeTensors:self.interpreter]) {
        return nil;
    }
    
    return [self getOutputData:self.interpreter];
}

- (BOOL)allocateTensors:(TFLInterpreter *)interpreter {
    NSError *error = nil;
    if (![interpreter allocateTensorsWithError:&error]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"allocateTensorsWithError %@",error.localizedDescription]];
        return NO;
    }
    return YES;
}

- (BOOL)setupInputData:(NSData *)inputData interpreter:(TFLInterpreter *)interpreter {
    NSError *error = nil;
    TFLTensor *inputTensor = [interpreter inputTensorAtIndex:0 error:&error];
    if (!inputTensor ||
        error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"inputTensor get \
                                          error message  %@",
                                          error.localizedDescription]];
        return NO;
    }
    [inputTensor copyData:inputData error:&error];
    if (error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"inputTensor copyData \
                                          error message  %@",
                                          error.localizedDescription]];
        return NO;
    }
    return YES;
}

- (BOOL)invokeTensors:(TFLInterpreter *)interpreter {
    NSError *error = nil;
    if (![interpreter invokeWithError:&error]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"allocateTensorsWithError %@",
                                          error.localizedDescription]];
        return NO;
    }
    return YES;
}

- (NSData *)getOutputData:(TFLInterpreter *)interpreter {
    NSError *error = nil;
    NSData *outData = nil;
    TFLTensor *outputTensor = [interpreter outputTensorAtIndex:0 error:&error];
    if (!outputTensor ||
        error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outputTensor \
                                          error message  %@",
                                          error.localizedDescription]];
        return outData;
    }
    
    outData = [outputTensor dataWithError:&error];
    if (error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outData error message  %@",error.localizedDescription]];
        return nil;
    }
    return outData;
}


- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.logger;
}

#pragma getter and setter

- (AoElogger *)logger {
    if (!_logger) {
        _logger = [[AoElogger alloc] initWithTag:NSStringFromClass([self class])];
    }
    return _logger;
}

@end
