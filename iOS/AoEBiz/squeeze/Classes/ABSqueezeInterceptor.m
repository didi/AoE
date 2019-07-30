//
//  ABMnistInterceptor.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/4.
//

#import "ABSqueezeInterceptor.h"
#import "ABSqueezeModelOption.h"
#import "AoEGraphicUtil.h"
#import "ABNCNNAdapter.h"
#import <AoE/AoElogger.h>

#define INPUT_BLOCK_SIZE 227

@interface ABSqueezeInterceptor ()
@property(nonatomic ,strong) ABNCNNAdapter *interpreter;
@property(nonatomic ,strong) AoElogger *logger;

@end

@implementation ABSqueezeInterceptor

- (void)close {
    self.interpreter = nil;
}

- (BOOL)isReady {
    return (self.interpreter != nil);
}

- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input {
    if ([self isReady]) {
        CVImageBufferRef inputBuffer = [self preProccessInput:input];
        id<AoEOutputModelProtocol> resultString = [self preProccessOutput:[self.interpreter
                                                                           imageClassification:inputBuffer
                                                                           blockSize:INPUT_BLOCK_SIZE]];
        return (id<AoEOutputModelProtocol>)resultString;
    }
    
    [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not ready error message"]];
    return nil;
}

- (CVImageBufferRef)preProccessInput:(id<AoEInputModelProtocol>)input {
    UIImage *image = (UIImage *)input;
    // 模型需要使用227 * 227 的输入 输入的格式BGR
    CVImageBufferRef buffer = [AoEGraphicUtil pixelBufferFromImage:image];
    CVImageBufferRef resizebuffer = [AoEGraphicUtil resizeImageBuffer:buffer
                                                                react:CGSizeMake(INPUT_BLOCK_SIZE, INPUT_BLOCK_SIZE)];
    CVPixelBufferRelease(buffer);
    return resizebuffer;
}

- (id<AoEOutputModelProtocol>)preProccessOutput:(NSString *)outputString {
    return (id <AoEOutputModelProtocol>)outputString;
}

- (BOOL)setupModel:(NSArray<ABSqueezeModelOption *> *)options {
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough option"]];
        return NO;
    }
    ABSqueezeModelOption *option = options.firstObject;
    NSError *error = nil;
    self.interpreter = [[ABNCNNAdapter alloc] initWithPath:option.modelDirPath name:option.modelFileName];
    if (error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"init fail %@",error.localizedDescription]];
        return NO;
    }
    return YES;
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
