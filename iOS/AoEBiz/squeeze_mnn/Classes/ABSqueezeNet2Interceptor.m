//
//  ABMnistInterceptor.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/4.
//

#import "ABSqueezeNet2Interceptor.h"
#import "AoEGraphicUtil.h"
#import "ABSqueezeNet2ModelLoaderConfig.h"
#import <AoERuntime/ARMNNAdapter.h>
#import <AoE/AoEModelOption.h>
#import <AoE/AoElogger.h>
#import <AoE/AoECrypto.h>
#import <AoE/AoEValidJudge.h>

#define INPUT_BLOCK_SIZE 226
static float meanVals[] = {104.f, 117.f, 123.f};

@interface ABSqueezeNet2Interceptor ()

@property(nonatomic ,strong) ARMNNAdapter *adapter;
@property(nonatomic ,strong) AoElogger *logger;
@property(nonatomic ,strong) NSArray<NSString *> *squeezenetWords;

@end

@implementation ABSqueezeNet2Interceptor

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
        NSArray *outDatas = nil;
        if (self.adapter &&
            [self.adapter respondsToSelector:@selector(run:)]) {
            outDatas = [self.adapter run:@[inputData]];
        }
        
        if (!outDatas ||
            outDatas.count < 1) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outData \
                                              error message  %@",
                                              error.localizedDescription]];
            return nil;
        }
        return [self preProccessOutput:outDatas];
    }
    [[self loggerComponent] errorLog:[NSString stringWithFormat:@"TFLInterpreter instance is not ready"]];
    return nil;
}

- (NSData *)preProccessInput:(id<AoEInputModelProtocol>)input {
    UIImage *image = (UIImage *)input;
//    int w               = image.size.width;
//    int h               = image.size.height;
//    unsigned char *rgba = (unsigned char *)calloc(w * h * 4, sizeof(unsigned char));
//    {
//        CGColorSpaceRef colorSpace = CGImageGetColorSpace(image.CGImage);
//        CGContextRef contextRef    = CGBitmapContextCreate(rgba, w, h, 8, w * 4, colorSpace,
//                                                           kCGImageAlphaNoneSkipLast | kCGBitmapByteOrderDefault);
//        CGContextDrawImage(contextRef, CGRectMake(0, 0, w, h), image.CGImage);
//        CGContextRelease(contextRef);
//    }
    
    // 模型需要使用226 * 226 的输入 输入的格式RGBA
    CVImageBufferRef buffer = [AoEGraphicUtil pixelBufferFromImage:image];
    CVImageBufferRef resizebuffer = [AoEGraphicUtil resizeImageBuffer:buffer
                                                                react:CGSizeMake(INPUT_BLOCK_SIZE, INPUT_BLOCK_SIZE)];
    CVPixelBufferRelease(buffer);
    
    size_t bufferLengh = CVPixelBufferGetDataSize(resizebuffer);
    self.adapter.blockSize = CGSizeMake(INPUT_BLOCK_SIZE, INPUT_BLOCK_SIZE);
    self.adapter.sourceSize =  CGSizeMake(CVPixelBufferGetWidth(resizebuffer), CVPixelBufferGetHeight(resizebuffer)); //CGSizeMake(w, h);//
    self.adapter.meanVals = meanVals;
    self.adapter.normVals = 0;
    self.adapter.sourceFormat = MNNAdapterImageFormatForRGBA;
    self.adapter.targetFormat = MNNAdapterImageFormatForBGR;
    NSData *inputData = [NSData dataWithBytes:[AoEGraphicUtil RGBA8BitmapFromBuffer:resizebuffer] length:bufferLengh];
    return inputData;
}

- (id<AoEOutputModelProtocol>)preProccessOutput:(NSArray *)outputData {
    if (!outputData ||
        !([outputData isKindOfClass:[NSArray class]])) {
        return nil;
    }
    
    NSArray *sort = outputData.firstObject;
    int top_class = 0;
    float max_score = 0.f;
    for (size_t i=0; i<sort.count; i++) {
        float score = ((NSNumber *)((NSArray *)sort[i]).firstObject).floatValue;
        if (score > max_score) {
            top_class = (int)i;
            max_score = score;
        }
    }

    NSString *word = self.squeezenetWords[top_class];
    
    return (id<AoEOutputModelProtocol>)[NSString stringWithFormat:@"%@   = %.2f",word,max_score];
}

- (BOOL)setupModel:(NSArray<AoEModelOption *> *)options {
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough options"]];
        return NO;
    }
    AoEModelOption *option = options.firstObject;
    NSString *fileName = [option.modelName stringByAppendingPathExtension:[ABSqueezeNet2ModelLoaderConfig ModelFileExtension]];
    NSString *modelPath = option.modelDirPath;
    self.adapter = [[ARMNNAdapter alloc] initWithPath:modelPath model:fileName];
    if (!self.adapter) {
        return NO;
    }else {
        NSError *error = nil;
        // 读取 synset_words.txt 作为分类类型
        NSString *filePath = [option.modelDirPath stringByAppendingPathComponent:@"synset_words.txt"];
        NSString *fileWords = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:&error];
        NSArray *squeezenet_words = [fileWords componentsSeparatedByString:@"\r\n"];
        if (squeezenet_words.count < 1) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"squeezenet_words init fail %@",error.localizedDescription]];
            return NO;
        }
        self.squeezenetWords = squeezenet_words;
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
