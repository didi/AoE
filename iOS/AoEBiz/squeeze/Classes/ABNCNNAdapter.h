//
//  ABNCNNAdapter.h
//  AoEBiz
//
//  Created by dingchao on 2019/4/3.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CoreVideo.h>

/**
 ncnn适配,现在包含了部分业务以后会抽离为单独的一层
 */
@interface ABNCNNAdapter : NSObject

/**
 用于记录输入图片为后期分析做准备
 */
@property (nonatomic , strong) UIImage *inputImage;

/**
 初始化方法

 @param path 模型文件所在路径
 @param name 模型文件名
 @return 类实例
 */
- (instancetype)initWithPath:(NSString *)path name:(NSString *)name;

/**
 Squeeze图形分类推理方法

 @param imageBuffer 输入图形
 @param img_block_size 处理图形大小
 @return 输出后的最后结果
 */
- (NSString *)imageClassification:(CVPixelBufferRef)imageBuffer blockSize:(int)img_block_size;
- (void)close;
@end

