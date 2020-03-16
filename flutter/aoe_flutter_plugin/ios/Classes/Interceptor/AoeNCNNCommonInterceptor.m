//
//  AoeNCNNCommonInterceptor.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/12.
//

#import "AoeNCNNCommonInterceptor.h"
#import "AoeFlutterInputModel.h"
#import "AoeFlutterModelOption.h"
#import <AoERuntime/ARNCNNAdapter.h>
#import <AoE/AoElogger.h>

@interface AoeNCNNCommonInterceptor ()
@property(nonatomic ,strong) ARNCNNAdapter *interpreter;
@property(nonatomic ,strong) AoElogger *logger;
@end

@implementation AoeNCNNCommonInterceptor

- (void)close {
    self.interpreter = nil;
}

- (BOOL)isReady {
    return (self.interpreter != nil);
}

- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.logger;
}

- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input {
    
    if (![self isReady]) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"ANCNNAdapter instance is not ready"]];
        return nil;
    }
    
    AoeFlutterInputModel * inputModel = (AoeFlutterInputModel *)input;
    self.interpreter.blockSize = inputModel.blockSize;
    self.interpreter.sourceSize = inputModel.sourceSize;
    self.interpreter.meanVals = inputModel.meanVals.bytes;
    self.interpreter.normVals = inputModel.normVals.bytes;
    
    NSArray *outputs = [self.interpreter run:@[inputModel.data]];
    if (outputs.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"out put error"]];
        return nil;
    }
    
    return (id<AoEOutputModelProtocol>)outputs;
    
}

- (BOOL)setupModel:(NSArray<id<AoEModelOptionProtocol>> *)options {
    
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough option"]];
        return NO;
    }
    
    AoeFlutterModelOption *option = (AoeFlutterModelOption *)options.firstObject;
    
    self.interpreter = [[ARNCNNAdapter alloc] initWithPath:option.modelDirPath param:option.modelParamFileName model:option.modelFileName];
    return self.interpreter != nil;
    
}

#pragma getter and setter

- (AoElogger *)logger {
    if (!_logger) {
        _logger = [[AoElogger alloc] initWithTag:NSStringFromClass([self class])];
    }
    return _logger;
}

@end
