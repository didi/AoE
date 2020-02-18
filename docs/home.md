---
layout: main
title: "AoE"
---

## 一、简介

**AoE** (AI on Edge) 是一个开源的 **终端侧 AI 集成运行时环境 ( IRE )**。帮助开发者将不同框架的深度学习算法轻松部署到终端高效执行。

## 二、使用文档&示例&资源
- [Android用户指南](./Android/README.md)
- [iOS用户指南](./iOS/README.md)
- [Android Demo](./Android/examples/demo)
- [iOS Demo](./iOS/Demo)
- 官方模型：
    * [SeesawNet_pytorch](https://github.com/cvtower/seesawfacenet_pytorch) 人脸比对模型, [https://arxiv.org/abs/1908.09124](https://arxiv.org/abs/1908.09124)

## 三、适配信息

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
        1.1.2<br>
        （2019/12/25）
        </center>
        </td>
        <td>TensorFlow Lite</td>
        <td>
        [ Android ] com.didi.aoe.runtime-tensorflow-lite:1.1.2 <br>
        [   iOS   ] pod 'AoERuntime/TensorFlowLite','~> 1.1.2', <br>
        </td>
        <td>2.0.0</td>
    </tr>
    <tr>
        <td>PyTorch</td>
        <td>
        [ Android ] com.didi.aoe.runtime-pytorch:1.1.2
        </td>
        <td>1.3.1</td>
    </tr>
    <tr>
        <td>MNN</td>
        <td>
        [ Android ] com.didi.aoe.runtime-mnn:1.1.2 <br>
        [   iOS   ] pod 'AoERuntime/MNN','~> 1.1.2', <br>
        </td>
        <td>0.2.1.5</td>
    </tr>
    <tr>
        <td>NCNN</td>
        <td>
        [ Android ] com.didi.aoe.runtime-ncnn:1.1.2 <br>
        [   iOS   ] pod 'AoERuntime/NCNN','~> 1.1.2', <br>
        [  linux  ] runtime-ncnn:1.1.2
        </td>
        <td>20191113</td>
    </tr>
</table>

