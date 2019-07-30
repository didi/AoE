//
//  ABNCNNAdapter.m
//  AoEBiz
//
//  Created by dingchao on 2019/4/3.
//
#import "ABNCNNAdapter.h"
#include <string>
#include <vector>
#include <fstream>
#include <streambuf>
#import <ncnn/net.h>
#import <AoE/AoE.h>
#import "squeezenet_v1.1.id.h"
#import <CoreGraphics/CoreGraphics.h>
#import <UIKit/UIKit.h>
#import <AoE/AoEValidJudge.h>
#import "AoEGraphicUtil.h"

#define posit(x) std::max(std::min(x, 1.0f), 0.0f)

namespace aoe_ncnn {
    struct IONodeInfo {
        const char *inKey;
        const char *outKey;
    };
    
    struct DetectImageInfo : IONodeInfo {
        unsigned char *data;
        int imageWidth;
        int imageHeight;
        int targetWidth;
        int targetHeight;
        const float *mean_vals;
        const float *norm_vals;
    };
    
    struct DetectResultInfo {
        NSString *message;
        ncnn::Mat outer;
    };
}

static float meanVals[] = {104.f, 117.f, 123.f};

@interface ABNCNNAdapter ()
{
    ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
    ncnn::PoolAllocator g_workspace_pool_allocator;
    ncnn::Mat squeezenet_bin;
    ncnn::Mat squeezenet_param;
    
    ncnn::Net net;
    std::vector<std::string> squeezenet_words;
}
@end

@implementation ABNCNNAdapter

- (ABNCNNAdapter *)initWithPath:(NSString *)path name:(NSString *)name {
    if (![AoEValidJudge isValidString:path] ||
        ![AoEValidJudge isValidString:name]) {
        return nil;
    }
    self = [super init];
    if (self) {
        NSString *fileName = [name stringByDeletingPathExtension];
        
        ncnn::Option opt;
        opt.lightmode = true;
        opt.num_threads = 4;
        opt.blob_allocator = &g_blob_pool_allocator;
        opt.workspace_allocator = &g_workspace_pool_allocator;
        net.opt = opt;
        // 加载 param
        int param_ret = net.load_param_bin([self getParamPath:path name:fileName]);
        // 加载 模型
        int model_ret = net.load_model([self getmodelPath:path name:fileName]);
        // 读取 synset_words.txt 作为分类类型
        NSString *filePath = [path stringByAppendingPathComponent:@"synset_words.txt"];
        std::ifstream textStream([filePath cStringUsingEncoding:NSUTF8StringEncoding]);
        std::string words_buffer((std::istreambuf_iterator<char>(textStream)),
                        std::istreambuf_iterator<char>());
        
        squeezenet_words = split_string(words_buffer, "\n");
        if (param_ret != 0 ||
            model_ret != 0 ||
            squeezenet_words.size() < 1) {
            NSAssert(NO, @"init model fail");
            self = nil;
        }
    }
    return self;
}

- (void)dealloc {
    [self close];
}

- (void)close {
    net.clear();
}

- (NSString *)imageClassification:(CVPixelBufferRef)imageBuffer blockSize:(int)img_block_size {
    
    aoe_ncnn::DetectImageInfo info;
    info.imageWidth = (int)CVPixelBufferGetWidth(imageBuffer);
    info.imageHeight = (int)CVPixelBufferGetHeight(imageBuffer);
    info.targetWidth = img_block_size;
    info.targetHeight = img_block_size;
    info.mean_vals = [self getRGBSpaceMean_vals];
    info.data = [AoEGraphicUtil RGBA8BitmapFromBuffer:imageBuffer];
//    UIImage *debugImage = [AoEGraphicUtil RGBA8ImageFrombitmap:info.data size:CGSizeMake(info.targetWidth, info.targetHeight)];
    aoe_ncnn::DetectResultInfo result = [self graphicPrediction:info];
    // 接到模型处理数据后做数据处理
    ncnn::Mat outer = result.outer;
    free(info.data);
    std::vector<float> cls_scores;
    cls_scores.resize(outer.w);
    for (int j=0; j<outer.w; j++)
    {
        cls_scores[j] = outer[j];
    }
    // 排序获取到最大值，然后根据key获取分类名称返回信息。
    int top_class = 0;
    float max_score = 0.f;
    for (size_t i=0; i<cls_scores.size(); i++)
    {
        float score = cls_scores[i];
        if (score > max_score)
        {
            top_class = (int)i;
            max_score = score;
        }
    }
    
    const std::string& word = squeezenet_words[top_class];
    char tmp[32];
    sprintf(tmp, "%.3f", max_score);
    std::string result_str = std::string(word.c_str() + 10) + " = " + tmp;
    
    return [NSString stringWithUTF8String:result_str.c_str()];
}

#pragma mark - private

- (aoe_ncnn::DetectResultInfo)graphicPrediction:(aoe_ncnn::DetectImageInfo)info {
    aoe_ncnn::DetectResultInfo result ;
    if (info.data == NULL) {
        result.message = @"input bitmap data is NULL";
        return result;
    }
    ncnn::Mat img = ncnn::Mat::from_pixels(info.data,
                                                ncnn::Mat::PIXEL_RGBA2BGR,
                                                info.imageWidth,
                                                info.imageHeight);
    img.substract_mean_normalize(info.mean_vals, 0);
    
    ncnn::Extractor extractor = net.create_extractor();
    extractor.set_light_mode(true);
    extractor.set_num_threads(4);
    int status = -1;
    status = extractor.input(squeezenet_v1_1_param_id::BLOB_data, img);
    if (status != 0) {
        img.release();
        result.message = [NSString stringWithFormat:@"ncnn::Extractor input %s fail , status is %@",info.inKey,@(status)];
        return result;
    }
    ncnn::Mat outer = NULL;
    status = extractor.extract(squeezenet_v1_1_param_id::BLOB_prob, outer);

    if (status != 0) {
        img.release();
        result.message = [NSString stringWithFormat:@"ncnn::Extractor output %s fail , status is %@",info.outKey,@(status)];
        return result;
    }
    result.outer = outer;
    result.message = [NSString stringWithFormat:@"\
                      input w:%d, h:%d,c:%d\n \
                      output w:%d,h:%d, c:%d\n",
                      img.w,img.h,img.c,
                      outer.w,outer.h,outer.c];
    img.release();
    return result;
}

- (const float *)getRGBSpaceMean_vals {
    return meanVals;
}

- (const char *)getParamPath:(NSString *)path name:(NSString *)name {
    NSString *filePath = [path stringByAppendingPathComponent:name];
    return [[filePath stringByAppendingPathExtension:@"param.bin"] cStringUsingEncoding:NSUTF8StringEncoding];
}

- (const char *)getmodelPath:(NSString *)path name:(NSString *)name {
    NSString *filePath = [path stringByAppendingPathComponent:name];
    NSString *modelPath = [filePath stringByAppendingPathExtension:@"bin"];
    return [modelPath cStringUsingEncoding:NSUTF8StringEncoding];
}

static std::vector<std::string> split_string(const std::string& str, const std::string& delimiter)
{
    std::vector<std::string> strings;
    
    std::string::size_type pos = 0;
    std::string::size_type prev = 0;
    while ((pos = str.find(delimiter, prev)) != std::string::npos)
    {
        strings.push_back(str.substr(prev, pos - prev));
        prev = pos + 1;
    }
    
    // To get the last substring (or only, if delimiter is not found)
    strings.push_back(str.substr(prev));
    
    return strings;
}
@end
