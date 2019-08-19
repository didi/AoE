

# 核心概念

## 目录 ##

1. [AoeClient——用户交互入口](#AoeClient)
2. [client配置模型](#AoEClientOption)
3. [模型配置协议——定义配置内容](#AoEModelOptionProtocol)
4. [拦截器组件协议——实现模型数据处理](#AoEInterpreterComponentProtocol)
5. [模型加载器组件协议——实现模型加载逻辑](#AoEModelLoaderComponentProtocol)

## AoeClient ##
AoeClient 是AoE推理操作的唯一交互窗口，您需要在自己业务实现中创建并使用。 

```objc

/**
 AoEClient 初始化方法
 
 @param clientOption Client的配置
 @param mainModelDir 模型的主目录
 @param subsequentModelDirs 其他模型目录
 @return AoEClient实例
 */
- (instancetype)initWithClientOption:(AoEClientOption *)clientOption
                            ModelDir:(NSString *)mainModelDir
                              subDir:(NSArray <NSString *>*)subsequentModelDirs;


```

### 模型配置

调用`setupModel:`方法进行组件和模型初始化

```objc
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

```
模型配置状态码定义如下：

```objc
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
```
### 释放资源 close
与[模型配置](#模型配置)对应，调用`close`方法释放模型加载资源。

### 执行推理 process
执行推理只需要调用`process`，喂入输入数据，数据将由自定义[InterpreterComponent](#InterpreterComponent)实现代理处理。

```objc
/**
 执行推理模型
 
 @param input 推理模型输入
 @return 推理模型输出
 */
- (id<AoEOutputModelProtocol>)process:(id<AoEInputModelProtocol>)input;

```

## AoEClientOption ##

`AoEClient`初始化的配置类

可以配置的信息如下：

```objc
@interface AoEClientOption : NSObject

/**
 处理流的className
 */
@property (strong, nonatomic) NSString *processorClassName;
/**
 模型加载器的className
 */
@property (strong, nonatomic) NSString *modelOptionLoaderClassName;
/**
 模型处理器的className
 */
@property (strong, nonatomic) NSString *interpreterClassName;
/**
 日志模块加载的className
 */
@property (strong, nonatomic) NSString *loggerClassName;

- (instancetype)setModelOptionLoader:(Class)modelOptionLoader;
@end

```

## AoEModelOptionProtocol ##

模型配置的标准化协议接口，AoE iOS SDK 标准模型配置规范是在模型目录下定义模型配置文件，说明模型加载方式和路径：

```objc
// demo 中使用的路径是通过AoEBiz的podspec配置好的
{resourcePath}/{bizBundle}/{bizName}/model.config

```

协议定义如下：

```objc
/**
 * 模型配置参数协议
 */
@protocol AoEModelOptionProtocol <NSObject>

- (NSString *)modelDirPath;
- (NSString *)modelName;
- (BOOL)isValidOption;

@end

```

AoE默认实现了一套模型配置标准，如果在初始化`AoEClientOption` 的时候没有指定`AoEModelLoaderComponent`类时，SDK会默认加载`Loader`子组件中的`AoEModelManager`类。**如果没有添加`loader`组件，SDK无法工作。**

```objc
@interface AoEModelOption : NSObject <AoEModelOptionProtocol, NSCopying>

/**
 模型版本
 */
@property (strong, nonatomic) NSString *version;

/**
 模型唯一索引
 */
@property (strong, nonatomic) NSString *tag;

/**
 模型文件所在文件夹
 */
@property (strong, nonatomic) NSString *modelDir;

/**
 模型名称
 */
@property (strong, nonatomic) NSString *modelName;

/**
 模型升级检查 url
 */
@property (strong, nonatomic) NSString *updateUrl;

/**
 下载后的模型地址
 */
@property (strong, nonatomic) NSString *modelPath;
/**
 模型文件签名
 */
@property (strong, nonatomic) NSString *sign;

+ (instancetype)modelWithPath:(NSString *)path;
- (instancetype)initWithPath:(NSString *)path;
- (instancetype)initWithDictionary:(NSDictionary *)opt;

@end

```

允许自定义模型定义文件格式，只需遵守 `AoEModelOptionProtocol` 协议即可，并需要配套实现[AoEModelLoaderComponentProtocol](#ModelOptionLoaderComponent)协议的组件，自行解析模型描述。具体用法可参见[demo](./AoEBiz/squeeze/)


## AoEInterpreterComponentProtocol
推理框架执行包装组件接口，负责完成模型文件的加载，推理操作执行，数据预处理、后处理转换，为推理操作的直接代理实现。需实现该接口，并显式注入到AoeClient。

```objc
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
```


## AoEModelLoaderComponentProtocol ##
对应[AoEModelOptionProtocol](#AoEModelOptionProtocol)实现的加载器组件接口，用于拓展数据模型加载协议。

```objc
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
```
