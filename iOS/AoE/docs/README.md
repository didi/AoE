
# AoE #

iOS 组件

# AoE Core #

Core 子模块实现了 AoE 核心框架流程。

# AoE Loader #

Loader 子模块提供了AoE默认的模型配置加载组件。

## AoEModelManager ##

遵循模型加载器组件协议 `AoEModelLoaderComponentProtocol ` 的实现类。

支持的模型配置文件为 `AoEModelOption ` 类。

## AoEModelOption ##

遵循模型配置文件协议 `AoEModelOptionProtocol ` 的实现类

```
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
支持的文件内容格式如下：

```
{
      "version": "1.0.0",
      "tag": "tag_mnist",
      "runtime": "tensorflow",
      "source": "installed",
      "modelDir": "mnist",
      "modelName": "mnist_cnn_keras"
}
```

## AoEModelLoaderConfigProtocol ##

用来定义加载器配置的协议，主要定义了下面的接口。

```
@protocol AoEModelLoaderConfigProtocol <NSObject>


/**
 sdk 本地存储位置

 @return 本地存储位置
 */
+ (NSString *)SDKLocalPath;

/**
 模型存储位置

 @return 模型存储位置
 */
+ (NSString *)ModelsDirLocalPath;

/**
 单个模型存储位置

 @param tag 模型存储唯一值
 @param alias 别名
 @return 单个模型存储位置
 */
+ (NSString *)modelLocalPathForTag:(NSString *)tag alias:(NSString *)alias;

/**
 模型配置文件名

 @return 模型配置文件名
 */
+ (NSString *)ModelConfigFileName;

/**
 模型配置扩展名

 @return 模型配置扩展名
 */
+ (NSString *)ModelConfigFileExtension;

/**
 模型扩展名

 @return 模型扩展名
 */
+ (NSString *)ModelFileExtension;
@end

```

# AoE Logger #

Logger 子模块为AoE提供了默认日志组件。
