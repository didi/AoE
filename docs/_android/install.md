---
layout: page
title: "集成到Android工程"
order: 1
---

AoE Android SDK 适用于 `API 15+`

该库在JCenter公开托管，您可以在其中下载 AAR 包。要通过 Gradle 引用依赖，请确保根项目 build.gradle 文件中添加JCenter存储库：

```gradle
allprojects {
  repositories {
    jcenter()
  }
}
```
在组件 build.gradle 中添加依赖即可：

```gradle
implementation 'com.didi.aoe:library-core:{{ site.github_version }}' // AoE 核心库

implementation 'com.didi.aoe:runtime-tensorflow-lite:{{ site.github_version }}' // 使用TensorFlow模型 *.tflite
implementation 'com.didi.aoe:runtime-pytorch:{{ site.github_version }}' // 使用Pytorch模型 *.pt (torch.jit.save)
implementation 'com.didi.aoe:runtime-mnn:{{ site.github_version }}' // 使用MNN模型 *.mnn
implementation 'com.didi.aoe:runtime-ncnn:{{ site.github_version }}' // 使用NCMM模型 *.bin
```
