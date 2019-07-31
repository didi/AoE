
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[ ![API](https://img.shields.io/badge/API-15+-brightgreen.svg)](https://img.shields.io/badge/API-14+-brightgreen.svg)

# AoE Android 用户指南

## 简介
**AoE** 提供终端侧机器学习集成运行环境（IRE），实现方便拓展支持各种AI推理框架的运
行，对上层业务提供统一的、轻量级、解藕的接口，提供独立进程运行机制，保障推理服务和数
据处理实现对业务宿主的稳定运行不受影响。

## 文档
* [概念介绍](./Concept.md)
* [架构设计](./Architecture.md)
* [组件设计](./Component.md)
* [高级定制](./Advanced.md)
* [发布历史](./ReleaseNotes.md)

## 示例Demo

[Demo](./samples/demo/demo) 通过两个简单的例子，演示了如何集成 AoE SDK，使用不
同的推理框架和组件实现进行业务应用。


| 1. 基于 [TensorFLow Lite](https://www.tensorflow.org/lite/) 的 MNIST 手写数字识别 | 2. 基于 [NCNN](https://github.com/Tencent/ncnn) 的 SqueezeNet 物体识别 |
|---|---|
|  ![MNIST](./../images/mnist_android.jpeg) |![Squeeze](./../images/squeeze_android.jpeg)|


## 集成使用
### 1. 引用依赖
AoE Android SDK 适用于 `API 15+`

该库在JCenter公开托管，您可以在其中下载 AAR 包。要通过 Gradle 引用依赖，请确保根
项目 build.gradle 文件中添加JCenter存储库：

```
allprojects {
  repositories {
    jcenter()
  }
}
```
在组件 build.gradle 中添加依赖即可：

```
implementation 'com.didi.aoe:library-core:1.0.0'
```
### 2. 添加模型和描述文件
AoE 标准模型配置规范是在 assets 模型目录下定义模型描述文件 `model.config`，指明
模型加载方式和路径：
```
assets/{feature_name}/model.config
```
以 Json 格式定义模型配置细节，样例如下：
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

>Tips: 如需定制协议请参考文档 [高级定制](./Advanced.md)

之后将本地内置或云端加载的模型文件放置在描述文件指定的地方。

### 3. 实现 InterpreterComponent 接口

目前AoE支持三种组件：
* [InterpreterComponent](./CONCEPT.md#InterpreterComponent)
* [ModelOptionLoaderComponent](./CONCEPT.md#ModelOptionLoaderComponent)
* [ParcelComponent](./CONCEPT.md#ParcelComponent)

InterpreterComponent为 **必须** 实现的组件，用于完成模型加载和数据预处理、后处理；

以下代码片段演示了如何基于 TensorFlow Lite 实现 MNIST 图像数字识别功能的 
InterpreterComponent，我们提供TensorFlow的运行时组件，使用了抽象类 
TensorFlowLiteInterpreter 对标准实现做了封装，只需实现数据处理接口即可完成组件
定制。

```
public class MnistInterpreter extends TensorFlowLiteInterpreter<float[], Integer, float[], float[][]> {

    @Nullable
    @Override
    public float[] preProcess(@NonNull float[] input) {
        return input;
    }

    @Nullable
    @Override
    public Integer postProcess(@Nullable float[][] modelOutput) {
        if (modelOutput != null && modelOutput.length == 1) {
            for (int i = 0; i < modelOutput[0].length; i++) {
                if (Float.compare(modelOutput[0][i], 1f) == 0) {
                    return i;
                }
            }
        }

        return null;
    }

}
```
ModelOptionLoaderComponent 和 ParcelComponent，可根据业务实际情况进行接口实现
然后进行注册。

> 具体使用方法参见 [MNIST](./samples/demo/features/mnist) 应用示例和 
[高级定制](./Advanced.md)

### 4. 推理服务交互
构造AoeClient实例，显式声明InterpreterComponent实例，进行推理操作。
```
// 1 构造AoeClient交互实例
AoeClient client = new AoeClient(requireContext(), {Client ID，自定义},
                new AoeClient.Options()
                        .setInterpreter({Interpreter实例类})
                        .useRemoteService(false), // AoE默认使用独立进程执行Interpreter，可手动关闭使用该特性
                {模型配置assets文件夹路径});

// 2 模型初始化，加载模型文件
int statusCode = client.init();

if (client.isRunning()) {
    // 3 执行推理
    Object result = client.process(inputData);
}

// 4 释放资源
client.release();
```

## 开源依赖

[TensorFlow](https://github.com/tensorflow/tensorflow/blob/master/LICENSE) Google的机器学习推理框架。MNIST 功能演示如何使用 AoE 包装好的TensorFlow Lite组件，porting了mnist的示例工程。

[Gson](https://github.com/google/gson/blob/master/LICENSE) Google的Json序列化库，用于默认模型配置加载。

[Kryo](https://github.com/EsotericSoftware/kryo/blob/master/LICENSE.md) EsotericSoftware的对象序列化框架，用于跨进程通信时对象序列化传递。有比较好的性能，作为可选序列化组件。

[NCNN](https://github.com/Tencent/ncnn/blob/master/LICENSE.txt) 腾讯的神经网络前向计算框架。Squeeze 功能演示如何深度定制集成推理框架，porting了ncnn的示例工程。

[gradle-download-task](https://github.com/michel-kraemer/gradle-download-task/blob/master/LICENSE.txt) Gradle 文件下载插件，用于模型文件下载任务，减小工程体积。
