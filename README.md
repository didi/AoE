<div align="middle">
    <img alt="AoE Logo" src="./images/aoe_logo_01.png" width="320" align="middle">
</div>

[![Build Status](https://travis-ci.org/didi/AoE.svg?branch=master)](https://travis-ci.org/didi/AoE)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[![CocoaPods Compatible](https://img.shields.io/cocoapods/v/AoE.svg)](https://cocoapods.org/pods/AoE)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)

 [文档](https://didi.github.io/AoE/) |
 [发布记录](./CHANGELOG.md) |
 [路线图](./ROADMAP.md) |
 [English](./README_en.md)

## 一、背景
### 1. AoE 是什么?
**AoE** (AI on Edge，终端智能，边缘计算) 是一个滴滴开源的 **终端侧 AI 集成运行时环境 ( IRE )**。以 **“稳定性、易用性、安全性”** 为设计原则，帮助开发者将不同框架的深度学习算法轻松部署到终端高效执行。

为什么要做一个终端 AI 集成运行时框架？原因有两个：

* **框架多样性**，随着人工智能技术快速发展，这两年涌现出了许多运行在终端的推理框架，一方面给开发者带来更多选择，另外一方面也增加了将 AI 布署到终端的成本。
* **流程繁琐**，通过推理框架直接接入 AI 的流程比较繁琐，涉及到动态库接入、资源加载、前处理、后处理、资源释放、模型升级，以及如何保障稳定性等问题。

### 2. 终端推理框架一览

|序号|名称|开发者|开源时间(年)|描述|
|:-:|:-:|:-:|:-:|:-|
|1|TensorFlow Lite|Google|2017|TensorFlow Lite使用Android Neural Networks API，默认调用CPU，目前最新的版本已经支持GPU。|
|2|Core ML|Apple|2017|Core ML是2017年Apple公司在WWDC上与iOS11同时发布的移动端机器学习框架，底层使用Accelerate和Metal分别调用CPU和GPU。Core ML需要将你训练好的模型转化为Core ML model|
|3|Caffe2|Facebook|2017|Caffe2是facebook在2017年发布的一个跨平台的框架，不仅仅支持Windows，Linux，Macos三大桌面系统，也支持移动端iOS，Android，可以说是集训练和推理于一身。|
|4|NCNN|腾讯|2017|NCNN是2017年腾讯优图实验室开源的移动端框架，使用C++ 实现，支持Android和iOS两大平台。|
|5|Paddle-Mobile|百度|2017|Paddle-Mobile是2017年百度PaddlePaddle组织下的移动端深度学习开源框架，当时叫做mobile-deep-learning(MDL)。支持安卓和iOS平台，CPU和GPU使用，提供量化工具。|
|6|QNNPACK|Facebook|2018|QNNPACK是Facebook在2018年发布的int8量化低精度高性能开源框架，全称Quantized Neural Network PACKage，用于手机端神经网络计算的加速，已经被整合到PyTorch 1.0中，在Caffe2里就能直接使用。|
|7|MACE|小米|2018|MACE是2018年小米在开源中国开源世界高峰论坛中宣布开源的移动端框架，以OpenCL和汇编作为底层算子，提供了异构加速可以方便在不同的硬件上运行模型，同时支持各种框架的模型转换。|
|8|MNN|阿里巴巴|2019|MNN是2019年阿里开源的移动端框架，不依赖第三方计算库，使用汇编实现核心运算，支持Tensorflow、Caffe、ONNX等主流模型文件格式，支持CNN、RNN、GAN等常用网络。|


### 3. AoE如何支持各种推理框架
从本质上来说，无论是什么推理框架，它都不可少的包含下面 5 个处理过程，对这些推理过程进行抽象，是 AoE 支持各种推理框架的基础。目前，AoE 实现了两种推理框架 NCNN 和 TensorFlow Lite 的支持，如下以这两种推理框架为例，说明一下 5 个推理过程在各自推理框架里的形式。

<table border="1">
  <tr>
    <th>推理框架</th>
    <th>初始化</th>
    <th>前处理</th>
    <th>执行推理</th>
    <th>后处理</th>
    <th>释放资源</th>
  </tr>
  <tr>
    <td>NCNN</td>
    <td>
        <code>
            int load_param(const unsigned char* mem);
            int load_model(const unsigned char* mem);
        </code>
    </td>
    <td>
        <code>
            ...
        </code>
    </td>
    <td>
        <code>
            int input(const char* blob_name, const Mat& in);
            int extract(const char* blob_name, Mat& feat);
        </code>
    </td>
    <td>
        <code>
            ...
        </code>
    </td>
    <td>
        <code>
            void release();
        </code>
    </td>
  </tr>
  <tr>
    <td>TensorFlow Lite</td>
        <td>
        <code>
            public Interpreter(@NonNull ByteBuffer byteBuffer, Interpreter.Options options);
        </code>
    </td>
    <td>
        <code>
            ...
        </code>
    </td>
    <td>
        <code>
            public void run(Object input, Object output);
        </code>
    </td>
    <td>
        <code>
            ...
        </code>
    </td>
    <td>
        <code>
            public void close();
        </code>
    </td>
  </tr>
</table>


### 3. AoE支持哪些平台
目前，AoE 提供了 Android 和 iOS 的实现，Linux 平台运行时环境 SDK 正在紧锣密鼓地开发中，预计在 9 月底发布，方便智能终端设备上落地 AI 业务。

## 二、工作原理
### 1. 抽象推理框架的处理过程
前面已经介绍了，其实每个推理框架都必然包含推理过程所必须的 5 个过程，它们分别是初始化、前处理、执行推理、后处理、释放资源。对 AoE 集成运行环境来说，最基本的便是抽象推理框架的这 5 个处理过程，通过依赖倒置的设计，使得业务只依赖AoE的上层抽象，而不用关心具体推理框架的接入实现。这种设计带来的最大的好处是开发者随时可以添加新的推理框架，而不用修改以前业务接入的代码，做到了业务开发和AoE SDK开发完全解耦。

在 AoE SDK 中这一个抽象是 InterpreterComponent（用来处理模型的初始化、执行推理和释放资源）和 Convertor（用来处理模型输入的前处理和模型输出的后处理），InterpreterComponent具体接口如下：
```
/**
 * 模型翻译组件
 */
interface InterpreterComponent<TInput, TOutput> extends Component {
    /**
     * 初始化，推理框架加载模型资源
     *
     * @param context      上下文，用与服务绑定
     * @param modelOptions 模型配置列表
     * @return 推理框架加载
     */
    boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions);
 
    /**
     * 执行推理操作
     *
     * @param input 业务输入数据
     * @return 业务输出数据
     */
    @Nullable
    TOutput run(@NonNull TInput input);
 
    /**
     * 释放资源
     */
    void release();
 
    /**
     * 模型是否正确加载完成
     *
     * @return true，模型正确加载
     */
    boolean isReady();
}
```

Convertor 的具体接口如下：

```
interface Convertor<TInput, TOutput, TModelInput, TModelOutput> {
    /**
     * 数据预处理，将输入数据转换成模型输入数据
     *
     * @param input 业务输入数据
     * @return 模型输入数据
     */
    @Nullable
    TModelInput preProcess(@NonNull TInput input);
 
    /**
     * 数据后处理，将模型输出数据转换成业务输出数据
     *
     * @param modelOutput 模型输出数据
     * @return
     */
    @Nullable
    TOutput postProcess(@Nullable TModelOutput modelOutput);
}
```


## 2. 稳定性保障
众所周知，android平台开发一个重要的问题是机型适配，尤其是包含大量native操作的场景，机型适配的问题尤其重要，一旦应用在某款机型上面崩溃，造成的体验损害是巨大的。有数据表明，因为性能问题，移动App每天流失的活跃用户占比5%，这些流失的用户，6成的用户选择了沉默，不再使用应用，3成用户改投竞品，剩下的用户会直接卸载应用。因此，对于一个用户群庞大的移动应用来说，保证任何时候App主流程的可用性是一件最基本、最重要的事件。结合 AI 推理过程来看，不可避免地，会有大量的操作发生在Native过程中，不仅仅是推理操作，还有一些前处理和资源回收的操作也比较容易出现兼容问题。为此，AoE 运行时环境 SDK 为 Android 平台上开发了独立进程的机制，让native操作运行在独立进程中，同时保证了推理的稳定性和主进程的稳定性，即偶然性的崩溃不会影响后续的推理操作，且主进程任何时候不会崩溃。具体实现过程主要有三个部分：

*2.1 注册独立进程*

这个比较简单，在manifest中增加一个remote service组件，代码如下：
```
<application>
    <service
        android:name=".AoeProcessService"
        android:exported="false"
        android:process=":aoeProcessor" />
 
</application>
```

*2.2 异常重新绑定独立进程*

在推理时，如果发现RemoteService终止了，执行“bindService()”方法，重新启动RemoteService。
```
@Override
public Object run(@NonNull Object input) {
    if (isServiceRunning()) {
        ...(代码省略)//执行推理
    } else {
        bindService();//重启独立进程
    }
    return null;
}
```

*2.3 跨进程通信优化*

因为独立进程，必然涉及到跨进程通信，在跨进程通信里最大的问题是耗时损失，这里，有两个因素造成了耗时损失，一是传输耗时，二是序列化/反序列化耗时。相比较使用Binder机制的传输耗时，序列化/反序列化占了整个通信耗时的90%以上。由此可见，对序列化/反序列化的优化是跨进程通信优化的重点。对比了当下主流的序列化/反序列化工具，最终AoE集成运行环境使用了Kryo库进行序列化/反序列，以下是对比结果（数据来源：https://www.oschina.net/question/2306979_238282）。

![kryo performance](https://static.oschina.net/uploads/space/2015/0602/113016_gK3v_2306979.png)

## 三、MNIST集成示例
*1. 对TensorFlowLiteInterpreter的继承*

当我们要接入一个新的模型时，首先要确定的是这个模型运行在哪一个推理框架上，然后继承这个推理框架的InterpreterComponent实现，完成具体的业务流程。MNIST是运行在TF Lite框架上的模型，因此，我们继承AoE的TFLite的Interpreter实现，将输入数据转成模型的输入，再从模型的输出读取业务需要的数据，初始化、推理执行和资源回收沿用TensorFlowLiteInterpreter的默认实现。
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

*2. 运行时环境配置*

接入MNIST的第二个步骤是配置推理框架类型和模型相关参数，代码如下：
```
mClient = new AoeClient(requireContext(), "mnist",
        new AoeClient.Options()
                .setInterpreter(MnistInterpreter.class)/*
                .useRemoteService(false)*/,
        "mnist");
```

*3. 推理执行*

以下是MINST初始化推理框架、推理执行和资源回收的实现：
```
// 初始化推理框架
int resultCode = mClient.init();
// 推理执行
Object result = mClient.process(mSketchModel.getPixelData());
if (result instanceof Integer) {
    int num = (int) result;
    Log.d(TAG, "num: " + num);
    mResultTextView.setText((num == -1) ? "Not recognized." : String.valueOf(num));
}
// 资源回收
if (mClient != null) {
    mClient.release();
}

```

## 四、使用文档&示例
- [Android用户指南](./Android/README.md)
- [iOS用户指南](./iOS/README.md)
- [Android Demo](./Android/samples/demo)
- [iOS Demo](./iOS/Demo)
- [更多应用案例](./Catalog.md)

## 五、Q&A

* `欢迎直接提交 issues 和 PRs`

* QQ群号：815254379

    <img alt="AoE QQ交流群" src="./images/aoe_qq.jpeg" width="196">


## 六、项目成员
### 核心成员

[kuloud](https://github.com/Kuloud)、
[dingc](https://github.com/qtdc1229) 、
[coleman.zou](https://github.com/zouyuefu) 、
[yangke1120](https://github.com/yangke1120) 、
[tangjiaxu](https://github.com/shupiankuaile) 

## 七、友情链接
我们部门的另外一款开源作品 [Dokit](https://github.com/didi/DoraemonKit)，一款功能齐全的客户端（ iOS 、Android ）研发助手，你值得拥有 :)

