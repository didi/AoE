

# 核心概念
## AoeClient
AoeClient 是 AoE 推理操作的唯一交互窗口，您需要在自己业务实现中创建并使用。 

```

/**
 * @param context             全局上下文
 * @param clientId            区分业务实现的ID（自定义）
 * @param options             Client配置，用来指定组件实例和运行模式
 * @param mainModelDir        主模型配置目录
 * @param subsequentModelDirs 子模型配置目录（用于多模型融合场景）
 */
public AoeClient(@NonNull Context context,
                 @NonNull String clientId,
                 @NonNull Options options,
                 @NonNull String mainModelDir,
                 @Nullable String... subsequentModelDirs)

```

### 初始化 init
调用 `init()` 方法进行组件和模型初始化
```
/**
 * 初始化、加载模型文件
 *
 */
@StatusCode
public int init() 
```
初始化状态码定义如下：
```
public @interface StatusCode {
    int UNDEFINE = 0;
    /**
     * 模型描述文件读取错误
     */
    int CONFIG_PARSE_ERROR = 1;
    /**
     * 模型描述文件解析正常，准备加载模型
     */
    int MODEL_LOAD_READY = 2;
    /**
     * 模型文件加载错误
     */
    int MODEL_LOAD_ERROR = 3;
    /**
     * 模型加载完成
     */
    int MODEL_LOAD_OK = 4;

}
```
### 释放资源 release
调用 `release()` 释放模型加载资源。

### 判断推理服务是否为可用状态 isRunning
当模型描述文件、组件和模型都加载完成，可进行推理操作时，调用 `isRunning()` 返回 true 。

### 执行推理 process
在推理服务为可用时，执行 `process()` ，喂入输入数据，数据将由自定义[InterpreterComponent](#InterpreterComponent)实现执行处理。
```
@WorkerThread
@Nullable
public Object process(Object input)
```

## AoeModelOption
模型配置的标准化接口，AoE Android SDK 标准模型配置规范是在模型目录下定义模型描述文件，说明模型加载方式和路径：
```
assets/{feature_name}/model.config
```
默认是以 Json 格式定义 AoeModelOption 所需的字段内容。

允许自定义模型定义文件格式，只需拓展 *AoeModelOption* 接口定义，并配套实现注册[ModelOptionLoaderComponent](#ModelOptionLoaderComponent)，自行解析模型描述文件。具体用法可参见 [Demo](./samples/demo/extensions/squeeze-model-option-loader)
```
public interface AoeModelOption extends Serializable {

    String getModelDir();

    String getModelName();

    boolean isValid();
}
```


## InterpreterComponent
推理框架执行包装组件接口，负责完成模型文件的加载，推理操作执行，数据预处理、后处理转换，为推理操作的直接实现。必需实现该接口，并显式注入到AoeClient。

## ParcelComponent
对象序列化组件接口，默认使用 `对象序列化机制（object serialization）`，是 Java 语言内建的一种对象持久化方式。该方式比较通用但需要输入数据实现 `Serializable` 接口，且序列化效率比较低，推荐使用 [Kryo](./aoe/extensions/parcel-kryo) 拓展组件实现，大概增加 200k 包大小，但是不要求实现 Serializable 接口，且实测处理耗时约为原生对象序列化的 1/7。

## ModelOptionLoaderComponent
对应 [AoeModelOption](#AoeModelOption) 实现的加载器组件接口，用于拓展数据模型加载协议。
