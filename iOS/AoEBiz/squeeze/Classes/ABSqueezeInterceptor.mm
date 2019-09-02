//
//  ABMnistInterceptor.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/4.
//

#import "ABSqueezeInterceptor.h"
#import "ABSqueezeModelOption.h"
#import "AoEGraphicUtil.h"
#import <AoERuntime/ABNCNNAdapter.h>
#import <AoE/AoElogger.h>
#import "squeezenet_v1.1.id.h"

#define INPUT_BLOCK_SIZE 227
static float meanVals[] = {104.f, 117.f, 123.f};
//static float normVals[] = {0.f, 0.f, 0.f};
@interface ABSqueezeInterceptor ()
@property(nonatomic ,strong) ABNCNNAdapter *interpreter;
@property(nonatomic ,strong) AoElogger *logger;
@property(nonatomic ,strong) NSArray<NSString *> *squeezenetWords;
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
        NSData *inputData = [self preProccessInput:input];
        NSArray *outputs = [self.interpreter run:@[inputData]];
        if (outputs.count < 1) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"out put error"]];
            return nil;
        }
        NSString *resultString = [self preProccessOutput:outputs.firstObject];
        return (id<AoEOutputModelProtocol>)resultString;
    }
    
    [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not ready error message"]];
    return nil;
}

- (NSData *)preProccessInput:(id<AoEInputModelProtocol>)input {
    UIImage *image = (UIImage *)input;
    // 模型需要使用227 * 227 的输入 输入的格式BGR
    CVImageBufferRef buffer = [AoEGraphicUtil pixelBufferFromImage:image];
    CVImageBufferRef resizebuffer = [AoEGraphicUtil resizeImageBuffer:buffer
                                                                react:CGSizeMake(INPUT_BLOCK_SIZE, INPUT_BLOCK_SIZE)];
    CVPixelBufferRelease(buffer);
    
    size_t bufferLengh = CVPixelBufferGetDataSize(resizebuffer);
    self.interpreter.blockSize = CGSizeMake(INPUT_BLOCK_SIZE, INPUT_BLOCK_SIZE);
    self.interpreter.sourceSize = CGSizeMake(CVPixelBufferGetWidth(resizebuffer), CVPixelBufferGetHeight(resizebuffer));
    self.interpreter.meanVals = meanVals;
    self.interpreter.normVals = 0;
    // [NSValue valueWithPointer:normVals];
    self.interpreter.inBlobIndex = squeezenet_v1_1_param_id::BLOB_data;
    self.interpreter.outBlobIndex = squeezenet_v1_1_param_id::BLOB_prob;
    NSData *inputData = [NSData dataWithBytes:[AoEGraphicUtil RGBA8BitmapFromBuffer:resizebuffer] length:bufferLengh];
    
    
    return inputData;
}

- (NSString *)preProccessOutput:(NSArray *)outputData {
    if (!outputData ||
        !([outputData isKindOfClass:[NSArray class]])) {
            return nil;
    }
    
    NSArray *sort = outputData.firstObject;
    int top_class = 0;
    float max_score = 0.f;
    for (size_t i=0; i<sort.count; i++)
    {
        float score = ((NSNumber *)sort[i]).floatValue;
        if (score > max_score) {
            top_class = (int)i;
            max_score = score;
        }
    }
    NSString *word = self.squeezenetWords[top_class];
    
    return [NSString stringWithFormat:@"%@   = %.3f",word,max_score];
}

- (BOOL)setupModel:(NSArray<ABSqueezeModelOption *> *)options {
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough option"]];
        return NO;
    }
    ABSqueezeModelOption *option = options.firstObject;
    NSError *error = nil;
    self.interpreter = [[ABNCNNAdapter alloc] initWithPath:option.modelDirPath param:option.modelParamFileName model:option.modelFileName];
    if (error) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"init fail %@",error.localizedDescription]];
        return NO;
    }else {
        // 读取 synset_words.txt 作为分类类型
        NSString *filePath = [option.modelDirPath stringByAppendingPathComponent:@"synset_words.txt"];
        NSString *fileWords = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:&error];
        NSArray *squeezenet_words = [fileWords componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]];
        if (squeezenet_words.count < 1) {
            [[self loggerComponent] errorLog:[NSString stringWithFormat:@"squeezenet_words init fail %@",error.localizedDescription]];
            return NO;
        }
        self.squeezenetWords = squeezenet_words;
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
