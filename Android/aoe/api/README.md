# AoE API #

此目录包含了AoE API定义，业务可根据组件接口定义拓展功能实现进行注册。

> 参考文档 [概念介绍](./../../../Concept.md)

## AoeModelOption

模型配置接口，用户需自定义一个模型描述文件，解析器提取出模型文件信息，用于配套 
Interpreter 加载解析模型文件。框架提供通用流程实现，可根据业务情况进行方便拓展。

标准模型配置规范是在模型目录下定义模型描述文件：
```
assets/{feature_name}/model.config
```
默认是以 Json 格式定义 AoeModelOption 所需的字段内容。

允许自定义模型定义文件格式，只需拓展 *AoeModelOption* 接口定义，并配套实现注册
[ModelOptionLoaderComponent](#ModelOptionLoaderComponent)，自行解析模型描述
文件。

```
public interface AoeModelOption extends Serializable {

    String getModelDir();

    String getModelName();

    boolean isValid();
}
```


> Tips: 示例项目 [Sequeeze](./../../../samples/demo/features/sequeeze)

## AoeProcessor

框架提供的核心控制接口，包括各组件接口定义。