
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[ ![API](https://img.shields.io/badge/API-15+-brightgreen.svg)](https://img.shields.io/badge/API-14+-brightgreen.svg)

# AoE Android 用户指南

### 环境要求
* 如果尚未安装，请按照网站上的说明安装 [Android Studio 3.5](https://developer.android.com/studio/index.html) 或更高版本。

* 最低 API 为 15 的 Android 设备和 Android 开发环境。

### 工程编译
* 打开Android Studio，然后从“欢迎”屏幕中选择 `Open an existing Android Studio project`。

* 在出现的 `Open File or Project` 窗口中，导航至克隆的 AoE GitHub 项目的存储位置，选择项目根目录下的 `Android` 目录。单击 `Ok`。

* 如果它要求您执行 `Gradle Sync`，请单击 `Ok`。

* 如果遇到诸如 `android-15` 之类的目标无法找到之类的错误，则可能还需要安装各种平台和工具。单击运行按钮（绿色箭头），或从顶部菜单中选择 运行 > 运行 'examples-demo'。您可能需要使用 `Build` > `Rebuild` 来重建项目。

* 另外，您需要插入一个启用了开发人员选项的 Android 设备。有关设置开发者设备的更多详细信息，请参见 [此处](https://developer.android.com/studio/run/device)。

### 使用模型
Demo 项目中使用的模型会通过 gradle/download.gradle 自动下载解压模型文件到对应  `assets` 目录。

## 集成使用
### 1. 引用依赖
AoE Android SDK 适用于 `API 15+`

该库在JCenter公开托管，您可以在其中下载 AAR 包。要通过 Gradle 引用依赖，请确保根
项目 build.gradle 文件中添加JCenter存储库：

```gradle
allprojects {
  repositories {
    // aoe maven 私仓[可选]
    maven {
      url 'https://dl.bintray.com/aoe/maven'
    }

    jcenter()
  }
}
```
在组件 build.gradle 中添加依赖即可：

```gradle
implementation 'com.didi.aoe:library-core:latest'
implementation 'com.didi.aoe:runtime-xxx:latest' // 依赖于选择的推理框架运行时
```

<table>
    <tr>
        <th>AoE 版本</th>
        <th>推理框架</th>
        <th>依赖信息</th>
        <th>推理框架版本</th>
    </tr>
    <tr>
        <td rowspan="4">
        <center>
        1.1.2 <br>
        （2019/12/25）
        </center>
        </td>
        <td>TensorFlow Lite</td>
        <td>
        com.didi.aoe.runtime-tensorflow-lite:1.1.2
        </td>
        <td>2.0.0</td>
    </tr>
    <tr>
        <td>PyTorch</td>
        <td>
        com.didi.aoe.runtime-pytorch:1.1.2
        </td>
        <td>1.3.1</td>
    </tr>
    <tr>
        <td>MNN</td>
        <td>
        com.didi.aoe.runtime-mnn:1.1.2
        </td>
        <td>0.2.1.5</td>
    </tr>
    <tr>
        <td>NCNN</td>
        <td>
        com.didi.aoe.runtime-ncnn:1.1.2
        </td>
        <td>20191113</td>
    </tr>
</table>

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

> 具体使用方法参见 [MNIST](./examples/demo/src/main/java/com/didi/aoe/features/mnist) 应用示例

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

