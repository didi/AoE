# AoE API | [ ![library-api](https://api.bintray.com/packages/aoe/maven/library-api/images/download.svg) ](https://bintray.com/aoe/maven/library-api/_latestVersion)

AoE 核心 API

## 使用

``` lang=gradle
implementation "com.didi.aoe:library-api:$aoe_version_name"
```

## 核心概念
## AoeModelOption

模型配置接口，AoE 标准模型配置规范是在模型目录下定义模型描述文件：
```
assets/{feature_name}/model.config
```
以 **Json** 格式定义 AoeModelOption 所需的字段内容。

> 可以自定义模型定义文件格式，只需拓展 *AoeModelOption* 接口定义，并配套实现注册
[ModelOptionLoaderComponent](#ModelOptionLoaderComponent)，自行解析模型描述
文件。

```
public interface AoeModelOption extends Serializable {

    /**
     * 模型文件文件夹路径
     *
     * @return 文件夹路径
     */
    @NonNull
    String getModelDir();

    /**
     * 模型文件名
     *
     * @return 模型文件名，不含路径
     */
    @NonNull
    String getModelName();

    @Nullable
    String getVersion();

    @NonNull
    @ModelSource
    String getModelSource();

    /**
     * 模型配置验证
     *
     * @return true，解析字段符合配置字段基本要求
     */
    boolean isValid();
}
```

## InterpreterComponent
推理引擎翻译组件，提供对模型文件的加载、推理动作执行，AoE 对应不同的推理引擎提供对应的封装实现，需由用户集成抽象类，实现数据预处理、后处理。

## Convertor
通过对应推理框架的 InterpreterComponent 抽象子类透传下来需要的接口定义，由用户自行实现输入数据到模型输入数据格式的转换，以及模型结果的处理逻辑。

``` lang=kotlin
interface Convertor<TInput, TOutput, TModelInput, TModelOutput> {
    /**
     * 数据预处理，将输入数据转换成模型输入数据
     *
     * @param input 业务输入数据
     * @return 模型输入数据
     */
    fun preProcess(input: TInput): TModelInput?

    /**
     * 数据后处理，将模型输出数据转换成业务输出数据
     *
     * @param modelOutput 模型输出数据
     * @return 业务输出数据
     */
    fun postProcess(modelOutput: TModelOutput?): TOutput?
}
```
