//
//  ABNCNNAdapter.h
//  AoEBiz
//
//  Created by dingchao on 2019/4/3.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CoreVideo.h>
#import <AoE/AEFrameworkAdapterProtocol.h>


/**
 ncnn适配,现在包含了部分业务以后会抽离为单独的一层
 */
@interface ABNCNNAdapter : NSObject <AEFrameworkAdapterProtocol>

@property (nonatomic ,assign) NSInteger inBlobIndex;
@property (nonatomic ,assign) NSInteger outBlobIndex;
@property (nonatomic ,assign) CGSize blockSize;
@property (nonatomic ,assign) CGSize sourceSize;
@property (nonatomic ,assign) const float *meanVals;
@property (nonatomic ,assign) const float *normVals;

/**
 初始化方法

 @param path 模型文件所在路径
 @param paramname 模型param文件名
 @param modelname 模型文件名
 @return 类实例
 */
- (instancetype)initWithPath:(NSString *)path param:(NSString *)paramname model:(NSString *)modelname;

- (void)close;
@end

