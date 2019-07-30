//
//  AoEClient.h
//  AoE
//
//  Created by dingchao on 2019/3/27.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

/**
 AoEClient 状态码

 - AoEClientStatusCodeForUndefine: 默认
 - AoEClientStatusCodeForLoaderNotExist:   模型加载器失败
 - AoEClientStatusCodeForConfigParseError: 模型配置读取错误
 - AoEClientStatusCodeForModelConfigReady: 配置文件已准备好
 - AoEClientStatusCodeForModelLoadError:   模型文件加载错误
 - AoEClientStatusCodeForModelLoadAlready: 模型加载完成
 */
typedef NS_ENUM(NSUInteger, AoEClientStatusCode) {
    AoEClientStatusCodeForUndefine = 0,
    AoEClientStatusCodeForLoaderNotExist,       // 模型加载器失败
    AoEClientStatusCodeForConfigParseError,     // 模型配置读取错误
    AoEClientStatusCodeForModelConfigReady,     // 模型配置加载正常，准备加载模型
    AoEClientStatusCodeForModelLoadError,       // 模型文件加载错误
    AoEClientStatusCodeForModelLoadAlready,     // 模型加载完成
};
@class AoEClient;
@class AoEClientOption;

typedef void (^AoEClientReadyBlock)(AoEClientStatusCode statusCode);
typedef id<AoEInputModelProtocol> (^AoEClientBeforeProcessBlock)(AoEClient *clinet, id<AoEInputModelProtocol>input);
typedef id<AoEOutputModelProtocol> (^AoEClientAfterProcessBlock)(AoEClient *clinet, id<AoEOutputModelProtocol>output);

@interface AoEClient : NSObject

@property (nonatomic , strong)AoEClientBeforeProcessBlock beforeProcessBlock;
@property (nonatomic , strong)AoEClientAfterProcessBlock afterProcessBlock;
/**
 AoEClient 初始化方法
 
 @param clientOption Client的配置
 @param mainModelDir 模型的主目录
 @param subsequentModelDirs 其他模型目录
 @return AoEClient实例
 */
- (instancetype)initWithClientOption:(AoEClientOption *)clientOption
                            modelDir:(NSString *)mainModelDir
                              subDir:(NSArray <NSString *>*)subsequentModelDirs;

/**
 设置推理模型
 setupModelImmediately:success:方法immediately参数为YES
 
 @param readyBlock 设置成功后回调block
 */
- (void)setupModel:(AoEClientReadyBlock)readyBlock;

/**
 设置推理模型
 
 @param immediately 可以延迟模型初始化到process之前。但是默认第一次process会返回为空。
 @param readyBlock 设置成功后回调block
 */
- (void)setupModelImmediately:(BOOL)immediately success:(AoEClientReadyBlock)readyBlock;

/**
 执行推理模型
 
 @param input 推理模型输入
 @return 推理模型输出
 */
- (id<AoEOutputModelProtocol>)process:(id<AoEInputModelProtocol>)input;

/**
停止推理
 
 */
- (void)close;

@end


