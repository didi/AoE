//
//  AoEProcessorProtocol.h
//  AoE
//
//  Created by dingchao on 2019/3/20.
//

#import <Foundation/Foundation.h>
#import "AoEModelOptionProtocol.h"
#import "AoEInputModelProtocol.h"
#import "AoEOutputModelProtocol.h"

@protocol AoEModelLoaderComponentProtocol;
@protocol AoEInterpreterComponentProtocol;
@protocol AoELoggerComponentProtocol;
@protocol AoEOutputModelProtocol;
@protocol AoEInputModelProtocol;
@class AoEClientOption;
/**
 拦截器管理协议
 */
@protocol AoEProcessorProtocol <NSObject>

- (instancetype)initWithClientOption:(AoEClientOption *)clientOption;
/**
 获取拦截器的实例即实际运行模型的实例

 @return 拦截器实例
 */
- (id<AoEInterpreterComponentProtocol>)getInterpreterComponent;
- (id<AoEModelLoaderComponentProtocol>)getModelLoaderComponent;

@optional
-(id<AoELoggerComponentProtocol>)loggerComponent;
@end


/**
 aoe组件基础接口协议
 */
@protocol AoEComponentProtocol <NSObject>

@optional
-(id<AoELoggerComponentProtocol>)loggerComponent;
@end

/**
 模型加载器组件接口协议
 */
@protocol AoEModelLoaderComponentProtocol <AoEComponentProtocol>

/**
 加载模型配置

 @param modelDir 模型dir
 @return 返回模型配置实例
 */
- (id<AoEModelOptionProtocol>)loadModelConfig:(NSString *)modelDir;

@end

/**
 拦截器（推理模型逻辑的实例）组件接口协议
 */
@protocol AoEInterpreterComponentProtocol <AoEComponentProtocol>

/**
 拦截器配置模型

 @param options 配置组
 @return 是否加载配置成功
 */
- (BOOL)setupModel:(NSArray<id<AoEModelOptionProtocol>>*)options;

/**
 拦截器运行模型推理方法

 @param input 模型输入
 @return 模型输出
 */
- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input;


/**
 实例停止运行
 */
- (void)close;
@optional
/**
 判断模型是否已准备完成

 @return 是否已准备完成
 */
- (BOOL)isReady;

@end

/**
 日志组件接口协议
 */
@protocol AoELoggerComponentProtocol <AoEComponentProtocol>

- (void)setLogTag:(NSString *)tag;
- (void)debugLog:(NSString *)content;
- (void)errorLog:(NSString *)content;
- (void)warningLog:(NSString *)content;
- (void)infoLog:(NSString *)content;
@end

/**
 加密组件接口协议
 */
@protocol AoECryptoComponentProtocol <AoEComponentProtocol>

- (NSData *)decryptModel:(NSData *)modelsData option:(id<AoEModelOptionProtocol>)option;

@optional
- (NSData *)encryptModel:(NSData *)modelsData option:(id<AoEModelOptionProtocol>)option;
@end
