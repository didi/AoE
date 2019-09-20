//
//  ARMNNAdapter.m
//  AoERuntime
//
//  Created by dingchao on 2019/9/3.
//
#import "ARMNNAdapter.h"
#import <AoE/AoE.h>
#import <CoreGraphics/CoreGraphics.h>
#import <UIKit/UIKit.h>

#import "AoEGraphicUtil.h"
#import "ARMNNTensor.h"

#include <vector>
#include <string>

#import <MNN/HalideRuntime.h>
#import <MNN/MNNDefine.h>
#import <MNN/ErrorCode.hpp>
#import <MNN/ImageProcess.hpp>
#import <MNN/Interpreter.hpp>
#import <MNN/Tensor.hpp>

typedef struct {
    float value;
    int index;
} LabeledElement;

static int CompareElements(const LabeledElement *a, const LabeledElement *b) {
    if (a->value > b->value) {
        return -1;
    } else if (a->value < b->value) {
        return 1;
    } else {
        return 0;
    }
}

namespace aoe_mnn {
    struct IONodeInfo {
        const char *inKey;
        const char *outKey;
        int outBlobIndex;
        int inBlobIndex;
    };
    
    struct imageInfo : IONodeInfo {
        unsigned char *data;
        int imageWidth;
        int imageHeight;
        int targetWidth;
        int targetHeight;
        const float *mean_vals;
        const float *norm_vals;
    };
    
    struct resultInfo : IONodeInfo {
        NSString *message;
        MNN::Tensor outer;
    };
}


@interface ARMNNAdapter ()
{
    std::shared_ptr<MNN::Interpreter> _net;
    MNN::Session *_session;
    std::vector<aoe_mnn::imageInfo> inputs;
    std::vector<aoe_mnn::resultInfo> outputs;
    
    BOOL isReady;
}
@end

@implementation ARMNNAdapter

- (ARMNNAdapter *)initWithPath:(NSString *)path model:(NSString *)modelname {
    isReady = NO;
    if (![AoEValidJudge isValidString:path] ||
        ![AoEValidJudge isValidString:modelname]) {
        return nil;
    }
    self = [super init];
    if (self) {
        _net            = std::shared_ptr<MNN::Interpreter>(MNN::Interpreter::createFromFile([self getcharPath:path name:modelname]));
        if (_net == nullptr) {
            NSAssert(NO, @"init model fail");
            self = nil;
        }else {
            isReady = YES;
        }
    }
    return self;
}
// TODO: add input tensor
- (void)addImage:(CVPixelBufferRef)imageBuffer blockSize:(CGSize)blockSize meanVals:(void *)meanVals norVals:(void *)norVals {
    aoe_mnn::imageInfo info;
    info.imageWidth = (int)CVPixelBufferGetWidth(imageBuffer);
    info.imageHeight = (int)CVPixelBufferGetHeight(imageBuffer);
    info.targetWidth = blockSize.width;
    info.targetHeight = blockSize.height;
    info.mean_vals = (const float *)meanVals;
    info.norm_vals = (const float *)norVals;
    info.data = [AoEGraphicUtil RGBA8BitmapFromBuffer:imageBuffer];
}

- (BOOL)isReady {
    return isReady;
}

- (NSArray *)run:(NSArray *)input {
    if (!input ||
        ![input isKindOfClass:[NSArray class]] ||
        input.count < 1) {
        return nil;
    }
    
    std::vector<aoe_mnn::imageInfo> infos;
    [self getInputInfos:&infos inputs:input];
    NSArray *outputs = nil;
    if (input) {
        outputs = [self invork:infos];
    }
    return outputs;
}

- (void)getInputInfos:(std::vector<aoe_mnn::imageInfo> *)infos inputs:(NSArray *)datas {
    std::vector<aoe_mnn::imageInfo> vector;
    for (int i = 0; i < datas.count; i++) {
        NSData *data = datas[i];
        aoe_mnn::imageInfo info;
        info.imageWidth = (int)self.sourceSize.width;
        info.imageHeight = (int)self.sourceSize.height;
        info.targetWidth = self.blockSize.width;
        info.targetHeight = self.blockSize.height;
        info.inKey = self.inBlobKey ? [self.inBlobKey cStringUsingEncoding:NSUTF8StringEncoding] : nullptr;
        info.outKey = self.outBlobKey ? [self.outBlobKey cStringUsingEncoding:NSUTF8StringEncoding] : nullptr;
        info.mean_vals = self.meanVals;
        info.norm_vals = self.normVals;
        info.data = (unsigned char *)data.bytes;
        vector.push_back(info);
    }
    *infos = vector;
}

- (NSMutableArray *)processOutputData:(MNN::Tensor *)tensor {
    MNN::Tensor copy(tensor);
    tensor->copyToHostTensor(&copy);
    float *data = copy.host<float>();
    NSMutableArray *channel = [NSMutableArray arrayWithCapacity:tensor->channel()];
    for (int i = 0; i < tensor->channel(); i ++) {
        NSMutableArray *array = [NSMutableArray arrayWithCapacity:tensor->width() * tensor->height()];
        const float* ptr = data + (i * (tensor->width() * tensor->height()));
        for (int j = 0; j < tensor->height(); j ++) {
            for (int x = 0; x < tensor->width(); x ++) {
                float value = ptr[x];
                [array addObject:@(value)];
            }
            ptr += tensor->width();
        }
        [channel addObject:array];
    }
    return channel;
}

- (void)dealloc {
    [self close];
}

- (void)close {
    _net->releaseSession(_session);
    _net->releaseModel();
}

#pragma mark - private

- (NSArray *)invork:(std::vector<aoe_mnn::imageInfo>)infos {
    std::vector<aoe_mnn::resultInfo> vector;
    
    NSMutableArray *outputs = [NSMutableArray arrayWithCapacity:0];
    
    MNN::ScheduleConfig config;
    config.type      = MNN_FORWARD_AUTO;
    config.numThread = 4;
    if (_session) {
        _net->releaseSession(_session);
    }
    _session = _net->createSession(config);
    
    for (int i = 0;i < infos.size(); i++) {
        aoe_mnn::imageInfo info = infos[i] ;
        aoe_mnn::resultInfo result ;
        if (info.data == NULL) {
            result.message = @"input bitmap data is NULL";
            continue;
        }
        
        MNN::CV::ImageProcess::Config process;
        if (info.mean_vals != nullptr) {
          ::memcpy(process.mean, info.mean_vals, sizeof(float) * 3);
        }
        if (info.norm_vals != nullptr) {
            ::memcpy(process.normal, info.norm_vals, sizeof(float) * 3);
        }
        
        process.sourceFormat = (MNN::CV::ImageFormat)self.sourceFormat;
        process.destFormat   = (MNN::CV::ImageFormat)self.targetFormat;
        
        std::shared_ptr<MNN::CV::ImageProcess> pretreat(MNN::CV::ImageProcess::create(process));
        MNN::CV::Matrix matrix;
        matrix.postScale(((float)info.imageWidth) / (float)info.targetWidth, ((float)info.imageHeight) / (float)info.targetHeight);
        pretreat->setMatrix(matrix);
        
        auto input = _net->getSessionInput(_session, info.inKey);
        MNN::ErrorCode code = pretreat->convert(info.data, info.targetWidth, info.imageHeight, 0, input);
        if (code != MNN::NO_ERROR) {
            result.message = [NSString stringWithFormat:@"mnn::CV::ImageProcess convert input %d %s fail , status is %@",info.inBlobIndex,info.inKey,@(code)];
            continue;
        }
        
        
        
        code = _net->runSession(_session);
        if (code != MNN::NO_ERROR) {
            result.message = [NSString stringWithFormat:@"mnn::Interpreter runSession fail %d %s fail , status is %@",info.inBlobIndex,info.inKey,@(code)];
            continue;
        }

        MNN::Tensor *output = _net->getSessionOutput(_session, nullptr);
        NSArray *channel = [self processOutputData:output];
        [outputs addObject:channel];
    }
    return outputs;
}

- (const char *)getcharPath:(NSString *)path name:(NSString *)name {
    NSString *filePath = [path stringByAppendingPathComponent:name];
    return [filePath cStringUsingEncoding:NSUTF8StringEncoding];
}

@end
