//
//  ABMnistInterceptor.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/4.
//

#import "ABMnistInterceptor.h"
#import "AoEGraphicUtil.h"
#import "ABMnistModelLoaderConfig.h"
#import "ABTensorFlowAdapter.h"
#import <AoE/AoEModelOption.h>
#import <AoE/AoElogger.h>

@interface ABMnistInterceptor ()
@property(nonatomic ,strong) ABTensorFlowAdapter *adapter;
@property(nonatomic ,strong) AoElogger *logger;

@end

@implementation ABMnistInterceptor

- (void)close {
    self.adapter = nil;
}

- (BOOL)isReady {
    if (self.adapter &&
        [self.adapter respondsToSelector:@selector(isReady)]) {
        return [self.adapter isReady];
    }
    return NO;
}

- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.logger;
}

- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input {
    if ([self isReady]) {
        NSError *error = nil;
        NSData *inputData = [self preProccessInput:input];
        NSData *outData = nil;
        if (self.adapter &&
            [self.adapter respondsToSelector:@selector(run:)]) {
            outData = [self.adapter run:inputData];
        }
        
        if (!outData) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outData \
                                              error message  %@",
                                              error.localizedDescription]];
            return nil;
        }
        return [self preProccessOutput:outData];
    }
    [[self loggerComponent] errorLog:[NSString stringWithFormat:@"TFLInterpreter instance is not ready"]];
    return nil;
}

- (NSData *)preProccessInput:(id<AoEInputModelProtocol>)input {
    // 需要的输入是28 * 28 ARBG 拿到的image格式是 RGBA 大小根据不同的手机不同。所以需要做转换
    UIImage *image = (UIImage *)input;
    CVImageBufferRef buffer = [AoEGraphicUtil pixelBufferFromImage:image];
    CVImageBufferRef scaledbuffer = [AoEGraphicUtil resizeImageBuffer:buffer react:CGSizeMake(28, 28)];
    CVPixelBufferRelease(buffer);
    CVPixelBufferLockBaseAddress(scaledbuffer, 0);
    unsigned char *newBitmap = NULL;
    unsigned char *bufferBase = CVPixelBufferGetBaseAddress(scaledbuffer);
    size_t bufferLengh = CVPixelBufferGetDataSize(scaledbuffer);
    newBitmap = (unsigned char *)malloc(sizeof(unsigned char) * bufferLengh);
    for (NSUInteger i = 0; i < bufferLengh / 4; i++) {
        NSUInteger byteIndex = i * 4;
        for (NSUInteger j = 0; j< 4; j++) {
            if (j == 3) {
                newBitmap[byteIndex] = bufferBase[byteIndex + j];
            }else {
                uint8_t pix = bufferBase[byteIndex + j];
                pix = pix & 0xff;
                newBitmap[byteIndex + j + 1] = 0xff - pix;//(pix > 0xef) ? 0 : 0xff;//
            }
        }
    }
    NSData *newImageData = [NSData dataWithBytes:newBitmap length:bufferLengh];
    CVPixelBufferUnlockBaseAddress(scaledbuffer, 0);
    CVPixelBufferRelease(scaledbuffer);
//    UIImage *debugimage = [AoEGraphicUtil RGBA8ImageFrombitmap:newBitmap size:CGSizeMake(28, 28)];
    free(newBitmap);
    return newImageData;
}

- (id<AoEOutputModelProtocol>)preProccessOutput:(NSData *)outputData {
    NSNumber *confident = @(-1);
    if (!outputData ||
        ![outputData isKindOfClass:[NSData class]]) {
        return (id <AoEOutputModelProtocol>)confident;
    }
    float output[1][10];
    [outputData getBytes:output length:(sizeof(float) * 10)];
    for (int i = 0; i < 10; i++) {
        if ([@(output[0][i]) compare:@(1.f)] == 0) {
            return (id <AoEOutputModelProtocol>)@(i);
        }
    }
    return (id <AoEOutputModelProtocol>)confident;
}

- (BOOL)setupModel:(NSArray<AoEModelOption *> *)options {
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough options"]];
        return NO;
    }
    AoEModelOption *option = options.firstObject;
    NSString *fileName = [option.modelName stringByAppendingPathExtension:[ABMnistModelLoaderConfig ModelFileExtension]];
    NSString *modelPath = [option.modelDirPath stringByAppendingPathComponent:fileName];
    self.adapter = [[ABTensorFlowAdapter alloc] initWithPath:modelPath];
    if (!self.adapter) {
        return NO;
    }
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
