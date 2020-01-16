//
//  ARNCNNAdapter.m
//  AoERuntime
//
//  Created by dingchao on 2019/4/3.
//
#import "ARNCNNAdapter.h"
#import <AoE/AoE.h>
#import <CoreGraphics/CoreGraphics.h>
#import <UIKit/UIKit.h>
#import <AoE/AoEValidJudge.h>
#import "AoEGraphicUtil.h"
#import "ARNCNNTensor.h"
#import <ncnn/ncnn/net.h>

#include <vector>
#include <string>

namespace aoe_ncnn {
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
        ncnn::Mat outer;
    };
}


@interface ARNCNNAdapter ()
{
    ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
    ncnn::PoolAllocator g_workspace_pool_allocator;
    
    ncnn::Net net;
    std::vector<aoe_ncnn::imageInfo> inputs;
    std::vector<aoe_ncnn::resultInfo> outputs;
    
    BOOL isReady;
}
@end

@implementation ARNCNNAdapter

- (ARNCNNAdapter *)initWithPath:(NSString *)path param:(NSString *)paramname model:(NSString *)modelname {
    isReady = NO;
    if (![AoEValidJudge isValidString:path] ||
        ![AoEValidJudge isValidString:paramname] ||
        ![AoEValidJudge isValidString:modelname]) {
        return nil;
    }
    self = [super init];
    if (self) {
        ncnn::Option opt;
        opt.lightmode = true;
        opt.num_threads = 4;
        opt.blob_allocator = &g_blob_pool_allocator;
        opt.workspace_allocator = &g_workspace_pool_allocator;
        net.opt = opt;
        // 加载 param
        int param_ret = net.load_param_bin([self getcharPath:path name:paramname]);
        // 加载 模型
        int model_ret = net.load_model([self getcharPath:path name:modelname]);
        if (param_ret != 0 ||
            model_ret != 0) {
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
    aoe_ncnn::imageInfo info;
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
    std::vector<aoe_ncnn::imageInfo> infos;
    [self getInputInfos:&infos inputs:input];
    NSArray *outputs = nil;
    if (input) {
        std::vector<aoe_ncnn::resultInfo> results;
        [self invork:infos result:&results];
        outputs = [self getOutputData:results];
    }
    return outputs;
}

- (void)getInputInfos:(std::vector<aoe_ncnn::imageInfo> *)infos inputs:(NSArray *)datas {
    std::vector<aoe_ncnn::imageInfo> vector;
    for (int i = 0; i < datas.count; i++) {
        NSData *data = datas[i];
        aoe_ncnn::imageInfo info;
        info.imageWidth = (int)self.sourceSize.width;
        info.imageHeight = (int)self.sourceSize.height;
        info.targetWidth = self.blockSize.width;
        info.targetHeight = self.blockSize.height;
        info.inBlobIndex = (int)self.inBlobIndex;
        info.outBlobIndex = (int)self.outBlobIndex;
        info.mean_vals = self.meanVals;
        info.norm_vals = self.normVals;
        info.data = (unsigned char *)data.bytes;
        vector.push_back(info);
    }
    *infos = vector;
}

- (NSArray *)getOutputData:(std::vector<aoe_ncnn::resultInfo>)results {
    if (results.size() < 1) {
        return nil;
    }
    NSMutableArray *outputs = [NSMutableArray arrayWithCapacity:0];
    for (int y = 0; y < results.size(); y ++) {
        aoe_ncnn::resultInfo result = results[y];
        ncnn::Mat outer = result.outer;
        NSMutableArray *channel = [NSMutableArray arrayWithCapacity:outer.c];
        for (int i = 0; i < outer.c; i ++) {
            NSMutableArray *array = [NSMutableArray arrayWithCapacity:outer.h * outer.w];
            const float* ptr = outer.channel(i);
            for (int j = 0; j < outer.h; j ++) {
                for (int x = 0; x < outer.w; x ++) {
                    float value = outer[x];
                    [array addObject:@(value)];
                }
                ptr += outer.w;
            }
            [channel addObject:array];
        }
        [outputs addObject:channel];
    }
    return outputs;
}

- (void)dealloc {
    [self close];
}

- (void)close {
    net.clear();
}

#pragma mark - private

- (void)invork:(std::vector<aoe_ncnn::imageInfo>)infos result:(std::vector<aoe_ncnn::resultInfo> *)results {
    std::vector<aoe_ncnn::resultInfo> vector;
    
    if (infos.size() < 1) {
        return;
    }
    
    ncnn::Extractor extractor = net.create_extractor();
    extractor.set_light_mode(true);
    extractor.set_num_threads(4);
    
    for (int i = 0;i < infos.size(); i++) {
        aoe_ncnn::imageInfo info = infos[i] ;
        aoe_ncnn::resultInfo result ;
        if (info.data == NULL) {
            result.message = @"input bitmap data is NULL";
            vector.push_back(result);
            continue;
        }
        ncnn::Mat img = ncnn::Mat::from_pixels(info.data,
                                               ncnn::Mat::PIXEL_RGBA2BGR,
                                               info.imageWidth,
                                               info.imageHeight);
        img.substract_mean_normalize(info.mean_vals, info.norm_vals);
        
        int status = -1;
        status = extractor.input(info.inBlobIndex, img);
        if (status != 0) {
            img.release();
            result.message = [NSString stringWithFormat:@"ncnn::Extractor input %d %s fail , status is %@",info.inBlobIndex,info.inKey,@(status)];
            vector.push_back(result);
            continue;
        }
        ncnn::Mat outer = NULL;
        status = extractor.extract(info.outBlobIndex , outer);
        if (status != 0) {
            img.release();
            result.message = [NSString stringWithFormat:@"ncnn::Extractor output %d %s fail , status is %@",info.outBlobIndex,info.outKey,@(status)];
            vector.push_back(result);
            continue;
        }
        result.outer = outer;
        result.message = [NSString stringWithFormat:@"\
                          input w:%d, h:%d,c:%d\n \
                          output w:%d,h:%d, c:%d\n",
                          img.w,img.h,img.c,
                          outer.w,outer.h,outer.c];
        vector.push_back(result);
        img.release();
    }
    *results = vector;
    
}

- (const char *)getcharPath:(NSString *)path name:(NSString *)name {
    NSString *filePath = [path stringByAppendingPathComponent:name];
    return [filePath cStringUsingEncoding:NSUTF8StringEncoding];
}

@end
