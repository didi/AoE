
# AoEBiz #

AoEBiz 组件记录了业务组件相关的功能实现。

## AoEBiz 组件 ##

该组件包含三个子组件 `[Core、mnist、squeeze]`

### Core ###

主要包含了图像处理的功能为其他的两个子组件提供基础服务。

### mnist ###

依赖Core子组件和TensorFlowLiteObjC。通过TensorFlowLiteObjC实现了mnist模型的推理。

#### ABMnistInterceptor

手写识别实现推理实现类。

遵循 `AoEInterpreterComponentProtocol` 协议，通过`TensorFlow lite` 框架执行模型推理。


### squeeze ###

主要包含了图像处理的功能为其他的两个子组件提供基础服务。

#### ABSqueezeInterceptor

物体识别实现推理实现类。

遵循 `AoEInterpreterComponentProtocol` 协议，通过`NCNN` 框架执行模型推理。

#### ABSqueezeModelOption

由于模型配置文件使用了与 `AoEModelOption` 不相同的结构，新增了多个字段。所以新添加了新的模型配置文件类，并且使用对应的模型加载器。

#### ABSqueezeModelManager

考虑到模型配置文件大部分和 `AoEModelOption `相同，所以新的模型加载器`ABSqueezeModelManager `直接继承了 `AoEModelManager `类。只处理不一样的部分即可，所以本身支持对`AoEModelOption `配置文件的扩展。

## AoEBiz mnist ##

使用 TensorFlow lite 实现手写识别模型推理。

## AoEBiz squeeze ##

SqueezeNet 物体识别功能。

## AoEBiz scrpt ##

用于组件需要的执行脚本

先行只有拉取模型的功能，在执行 `pod install` 的时候会自动拉取手写功能与物体识别功能需要的模型文件，模型存储在滴滴云中，本身是开源模型，请大家放心使用
