
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[ ![API](https://img.shields.io/badge/API-15+-brightgreen.svg)](https://img.shields.io/badge/API-14+-brightgreen.svg)

# AoE Android 用户指南

## 简介
**AoE** 提供终端侧机器学习集成运行环境（IRE），实现方便拓展支持各种AI推理框架的运
行，对上层业务提供统一的、轻量级、解藕的接口。


## 示例Demo
[Demo](./demo/demo) 通过几个简单的例子，演示了如何集成 AoE SDK，使用不同的推理框架和组件实现进行业务应用。

### 环境要求
* 如果尚未安装，请按照网站上的说明安装 [Android Studio 3.5](https://developer.android.com/studio/index.html) 或更高版本。

* 最低 API 为 15 的 Android 设备和 Android 开发环境。

### 工程编译
* 打开Android Studio，然后从“欢迎”屏幕中选择 `Open an existing Android Studio project`。

* 在出现的 `Open File or Project` 窗口中，导航至克隆的 AoE GitHub 项目的存储位置，选择项目根目录下的 `Android` 目录。单击 `Ok`。

* 如果它要求您执行 `Gradle Sync`，请单击 `Ok`。

* 如果遇到诸如 `android-15` 之类的目标无法找到之类的错误，则可能还需要安装各种平台和工具。单击运行按钮（绿色箭头），或从顶部菜单中选择 运行 > 运行 'examples-demo'。您可能需要使用 `Build` > `Rebuild` 来重建项目。

* 另外，您需要插入一个启用了开发人员选项的 Android 设备。有关设置开发者设备的更多详细信息，请参见 [此处](https://developer.android.com/studio/run/device)。

### Demo 模型介绍
|模型|推理框架|拓展组件|加载方式|
|:-|:-|:-|:-|
|MNIST|TensorFlow Lite| -[x]runtime-tensorflow-lite -[x]extensions-service|云端加载|
|Squeeze|NCNN|-[x]runtime-ncnn|内置模型|
|MobileNet V1|MNN|-[x]runtime-mnn|内置模型|

### 使用模型
Demo 项目中 `MNIST` 使用了云端加载方式从 AoE 后台同步/下载模型（请确保授予应用网络访问权限），而 `recognize` 和 `squeeze` 则直接使用脚本 `download-models.gradle` 自动下载解压模型文件到指定目录供本地离线使用。

如果您想直接下载模型和配置文件，可直接访问下载：

[recognize](https://img0.didiglobal.com/static/starfile/node20190826/895f1e95e30aba5dd56d6f2ccf768b57/eraqUlJwtE1566819400795.zip)
｜
[squeeze](https://img0.didiglobal.com/static/starfile/node20190805/895f1e95e30aba5dd56d6f2ccf768b57/fm2gKZ37I11565012061785.zip)



### 附加说明
请不要删除 `assets` 文件夹内容。如果您删除了文件，请从菜单中选择 `Build` > `Rebuild`，以将已删除的模型文件重新下载到 `assets` 文件夹中。

## 集成使用
### 1. 引用依赖
AoE Android SDK 适用于 `API 15+`

该库在JCenter公开托管，您可以在其中下载 AAR 包。要通过 Gradle 引用依赖，请确保根
项目 build.gradle 文件中添加JCenter存储库：

```
allprojects {
  repositories {
    maven {
      url 'https://dl.bintray.com/aoe/maven'
    }
    jcenter()
  }
}
```
在组件 build.gradle 中添加依赖即可：

```
implementation 'com.didi.aoe:library-core:latest'
implementation 'com.didi.aoe:runtime-xxx:latest' // 依赖于选择的推理框架运行时
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
      "runtime": "tensorflow",
      "source": "cloud",
      "modelId": 75,
      "modelDir": "mnist",
      "modelName": "mnist_cnn_keras.tflite",
      "modelSign":"822d72ce038baef3e40924016d4550bc"
}
```

### 3. 实现 InterpreterComponent 接口

InterpreterComponent 用于完成模型加载和数据预处理、后处理；

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

> 具体使用方法参见 [MNIST](./demo/features/mnist) 应用示例

### 4. 推理服务交互
构造AoeClient实例，显式声明InterpreterComponent实例，进行推理操作。
```
// 1 构造AoeClient交互实例
AoeClient client = new AoeClient(requireContext(),
                new AoeClient.Options()
                        .setInterpreter({Interpreter实例类})
                        .useRemoteService(false), // AoE默认使用独立进程执行Interpreter，可手动关闭使用该特性
                {模型配置assets文件夹路径});

// 2 模型初始化，加载模型文件
mClient.init(new AoeClient.OnInitListener() {
    @Override
    public void onSuccess() {
        super.onSuccess();
        Log.d(TAG, "AoeClient init success");
    }

    @Override
    public void onFailed(int code, String msg) {
        super.onFailed(code, msg);
        Log.d(TAG, "AoeClient init failed: " + code + ", " + msg);
    }
});

if (client.isRunning()) {
    // 3 执行推理
    Object result = client.process(inputData);
}

// 4 释放资源
client.release();
```

