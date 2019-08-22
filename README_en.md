<div align="middle">
    <img alt="AoE Logo" src="./images/aoe_logo_01.png" width="320" align="middle">
</div>

[![Build Status](https://travis-ci.org/didi/AoE.svg?branch=master)](https://travis-ci.org/didi/AoE)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[![CocoaPods Compatible](https://img.shields.io/cocoapods/v/AoE.svg)](https://cocoapods.org/pods/AoE)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)

 [Documentation](https://didi.github.io/AoE/) |
 [Release Note](./CHANGELOG.md) |
 [Road Map](./ROADMAP.md) |
 [中文](./README.md)

## I. Background
### 1. What is AoE?
**AoE** (AI on Edge) is a drop-open open source **terminal side AI Integrated Runtime Environment (IRE)**. Designed with **"stability, ease of use, security"** to help developers easily deploy deep learning algorithms from different frameworks to the terminal for efficient execution.

Why do you want to make a terminal AI integrated runtime framework? There are two reasons for this:

* **Frame diversity**, with the rapid development of artificial intelligence technology, in the past two years, there have been many inference frameworks running on the terminal, which on the one hand gives developers more choices, on the other hand, it also increases the AI. The cost of deploying to the terminal.
* **The process is cumbersome**. The process of directly accessing AI through the reasoning framework is cumbersome, involving dynamic library access, resource loading, pre-processing, post-processing, resource release, model upgrade, and how to ensure stability.

### 2. Overview of terminal reasoning framework

| Sequence Number | Name | Developer | Open Source Time (Year) | Description |
|:-:|:-:|:-:|:-:|:-|
|1|TensorFlow Lite|Google|2017|TensorFlow Lite uses the Android Neural Networks API, which calls the CPU by default. The latest version already supports GPU. |
|2|Core ML|Apple|2017|Core ML is a mobile machine learning framework released by Apple in WWDC and iOS11 in 2017. The underlying uses Accelerate and Metal to call CPU and GPU respectively. Core ML needs to convert your trained model into Core ML model|
|3|Caffe2|Facebook|2017|Caffe2 is a cross-platform framework that Facebook released in 2017. It not only supports Windows, Linux, Macos, but also supports mobile iOS, Android, and can be said to be training and Reasoning in one. |
|4|NCNN|Tencent|2017|NCNN is the open source mobile framework of Tencent U-Map Lab in 2017. It is implemented in C++ and supports both Android and iOS platforms. |
|5|Paddle-Mobile|Baidu|2017|Paddle-Mobile is a mobile-derived deep learning open source framework under the Baidu PaddlePaddle organization in 2017, called mobile-deep-learning (MDL). Support for Android and iOS platforms, CPU and GPU usage, providing quantitative tools. |
|6|QNNPACK|Facebook|2018|QNNPACK is the int8 quantization low-precision high-performance open source framework released by Facebook in 2018. The full name of Quantized Neural Network PACKage, which is used for the acceleration of mobile-side neural network computing, has been integrated into PyTorch 1.0. It can be used directly in Caffe2. |
|7|MACE|Xiaomi|2018|MACE is 2018 Xiaomi announced in the open source China Open Source World Summit Forum open source mobile framework, with OpenCL and assembly as the underlying operator, providing heterogeneous acceleration can be convenient on different hardware Run the model while supporting model transformations for various frameworks. |
|8|MNN|Alibaba|2019|MNN is the open-end mobile framework of Ali in 2019. It does not rely on third-party computing libraries, uses assembly to implement core operations, supports mainstream model file formats such as Tensorflow, Caffe, ONNX, and supports CNN and RNN. , GAN and other common networks. |


### 3. How AoE supports various reasoning frameworks
In essence, no matter what reasoning framework, it is indispensable to include the following five processes. Abstracting these reasoning processes is the basis for AoE to support various inference frameworks. At present, AoE implements the support of two inference frameworks, NCNN and TensorFlow Lite. The following two reasoning frameworks are used as examples to illustrate the form of the five inference processes in their respective reasoning frameworks.


<table border="1">
  <tr>
    <th>Inference Framework</th>
    <th>Initialization</th>
    <th>Pre-Processing</th>
    <th>Execution Reasoning</th>
    <th>post processing</th>
    <th>Release resources</th>
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


### 3. Which platforms does AoE support?
Currently, AoE provides Android and iOS implementations. The Linux platform runtime environment SDK is under intense development and is expected to be released at the end of September to facilitate the landing of AI services on smart devices.

## II. Working principle
### 1. The process of abstract reasoning framework
As mentioned above, in fact, each reasoning framework must contain the five processes necessary for the inference process, which are initialization, pre-processing, execution reasoning, post-processing, and release of resources. For the AoE integrated runtime environment, the most basic process is the five processes of the abstract reasoning framework. By relying on the inverted design, the business only relies on the upper layer abstraction of AoE, instead of the access implementation of the specific reasoning framework. The biggest benefit of this design is that developers can add new reasoning frameworks at any time without modifying the code of previous business access, so that business development and AoE SDK development are completely decoupled.

In the AoE SDK, this abstraction is InterpreterComponent (used to handle the initialization of the model, execution reasoning and release of resources) and Convertor (for post processing of model input and post-processing of model output). The specific interface of InterpreterComponent is as follows:

The specific interface of the Convertor is as follows:

```
/**
 * Model translation component
 */
interface InterpreterComponent<TInput, TOutput> extends Component {
    /**
     * Initialization, inference framework loading model resources
     *
     * @param context context, bound to the service
     * @param modelOptions Model configuration list
     * @return reasoning framework loading
     */
    boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions);
 
    /**
     * Perform inferential operations
     *
     * @param input business input data
     * @return business output data
     */
    @Nullable
    TOutput run(@NonNull TInput input);
 
    /**
     * Release resources
     */
    void release();
 
    /**
     * Whether the model is loaded correctly
     *
     * @return true，Model loading correctly
     */
    boolean isReady();
}
```

The specific interface of the Convertor is as follows:

```
interface Convertor<TInput, TOutput, TModelInput, TModelOutput> {
    /**
     * Data preprocessing to convert input data into model input data
     *
     * @param input Business input data
     * @return Model input data
     */
    @Nullable
    TModelInput preProcess(@NonNull TInput input);
 
    /**
     * Data post-processing to convert model output data into business output data
     *
     * @param modelOutput Model output data
     * @return
     */
    @Nullable
    TOutput postProcess(@Nullable TModelOutput modelOutput);
}
```


## 2. Stability guarantee
As we all know, an important problem in the development of the android platform is the model adaptation, especially the scenario involving a large number of native operations. The problem of model adaptation is especially important. Once the application crashes on a certain model, the damage caused by the experience is huge. . According to the data, because of the performance problem, mobile users lose 5% of active users every day. These lost users, 60% of users choose to silence, no longer use the application, 30% of users change their competitors, and the remaining users The app will be uninstalled directly. Therefore, for a mobile application with a large user base, ensuring the availability of the App main process at any time is the most basic and important event. In combination with the AI ​​reasoning process, inevitably, a large number of operations occur in the Native process, not just inference operations, but also some pre-processing and resource recycling operations are more prone to compatibility problems. To this end, the AoE Runtime Environment SDK develops a mechanism for independent processes on the Android platform, allowing native operations to run in independent processes, while ensuring the stability of reasoning and the stability of the main process, ie accidental crashes do not affect subsequent The reasoning operation, and the main process will not crash at any time. The specific implementation process has three main parts:

*2.1 Registration of independent processes*

This is relatively simple, add a remote service component in the manifest, the code is as follows:

```
<application>
    <service
        android:name=".AoeProcessService"
        android:exported="false"
        android:process=":aoeProcessor" />
 
</application>
```

*2.2 Exception rebinding independent process*

In the reasoning, if the RemoteService is found to be terminated, execute the "bindService()" method and restart the RemoteService.


```
@Override
public Object run(@NonNull Object input) {
    if (isServiceRunning()) {
        ... (code omitted) // execution reasoning
    } else {
        bindService();// Restart the independent process
    }
    return null;
}
```

*2.3 Cross-process communication optimization*

Because the independent process must involve cross-process communication, the biggest problem in cross-process communication is time-consuming loss. Here, two factors cause time-consuming loss, one is transmission time-consuming, and the other is serialization/deserialization. time consuming. Compared to the transmission time using the Binder mechanism, serialization/deserialization accounts for 90% of the total communication time. It can be seen that the optimization of serialization/deserialization is the focus of cross-process communication optimization. Comparing the current mainstream serialization/deserialization tools, the final AoE integrated runtime environment uses the Kryo library for serialization/inverse sequence. The following are the comparison results.

![kryo performance](https://static.oschina.net/uploads/space/2015/0602/113016_gK3v_2306979.png)

## III. MNIST integration example
*1. Inheritance to TensorFlowLiteInterpreter*

When we want to access a new model, we must first determine which reasoning framework the model runs on, and then inherit the InterpreterComponent implementation of the reasoning framework to complete the specific business process. MNIST is a model that runs on the TF Lite framework. Therefore, we inherit the Interpreter implementation of AoE's TFLite, convert the input data into the input of the model, and then read the data needed by the business from the output of the model. Initialization, inference execution, and Resource recycling follows the default implementation of TensorFlowLiteInterpreter.

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

*2. Runtime Environment Configuration*

The second step in accessing MNIST is to configure the inference framework type and model related parameters. The code is as follows:

```
mClient = new AoeClient(requireContext(), "mnist",
        new AoeClient.Options()
                .setInterpreter(MnistInterpreter.class)/*
                .useRemoteService(false)*/,
        "mnist");
```

*3. Reasoning execution*

The following is the implementation of MINST's initial reasoning framework, inferential execution, and resource recycling:

```
// Initialization inference framework
int resultCode = mClient.init();
// Inferential execution
Object result = mClient.process(mSketchModel.getPixelData());
if (result instanceof Integer) {
    int num = (int) result;
    Log.d(TAG, "num: " + num);
    mResultTextView.setText((num == -1) ? "Not recognized." : String.valueOf(num));
}
// Recycle
if (mClient != null) {
    mClient.release();
}

```

## IV. Working with documents & examples
- [Android User Guide](./Android/README.md)
- [iOS User Guide](./iOS/README.md)
- [Android Demo](./Android/samples/demo)
- [iOS Demo](./iOS/Demo)
- [More examples](./Catalog.md)

## V. Q&A

* `Welcome to submit issues and PRs directly`


## VI. Project members
### Core member

[kuloud](https://github.com/Kuloud)、
[dingc](https://github.com/qtdc1229) 、
[coleman.zou](https://github.com/zouyuefu) 、
[yangke1120](https://github.com/yangke1120) 、
[tangjiaxu](https://github.com/shupiankuaile) 

## Friendly link
Another open source project in our department [Dokit](https://github.com/didi/DoraemonKit), a full-featured client (iOS, Android) development assistant, you deserve :)


