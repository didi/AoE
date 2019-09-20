//
//  ARMNNAdapter.h
//  AoERuntime
//
//  Created by dingchao on 2019/9/3.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CoreVideo.h>
#import <AoE/AoE.h>

typedef NS_ENUM(NSUInteger, MNNAdapterImageFormat) {
    MNNAdapterImageFormatForRGBA = 0,
    MNNAdapterImageFormatForRGB,
    MNNAdapterImageFormatForBGR,
    MNNAdapterImageFormatForGRAY,
    MNNAdapterImageFormatForBGRA,
    MNNAdapterImageFormatForYUV_NV21 = 11,
};
/**
 ncnn适配,现在包含了部分业务以后会抽离为单独的一层
 */
@interface ARMNNAdapter : NSObject <AoEFrameworkAdapterProtocol>

@property (nonatomic ,copy) NSString *inBlobKey;
@property (nonatomic ,copy) NSString *outBlobKey;
@property (nonatomic ,assign) NSInteger sourceFormat;
@property (nonatomic ,assign) NSInteger targetFormat;
@property (nonatomic ,assign) CGSize blockSize;
@property (nonatomic ,assign) CGSize sourceSize;
@property (nonatomic ,assign) const float *meanVals;
@property (nonatomic ,assign) const float *normVals;

/**
 初始化方法

 @param path 模型文件所在路径
 @param modelname 模型文件名
 @return 类实例
 */
- (ARMNNAdapter *)initWithPath:(NSString *)path model:(NSString *)modelname;

- (void)close;
@end

