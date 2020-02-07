<div align="middle">
    <img alt="AoE Logo" src="https://img0.didiglobal.com/static/gstar/img/AwRPnWCJkW1577246025490.png" width="300" align="middle">
</div>

![](https://github.com/didi/aoe/workflows/Android%20CI/badge.svg)
[![Android](https://api.bintray.com/packages/aoe/maven/library-core/images/download.svg) ](https://bintray.com/aoe/maven/library-core/_latestVersion)
[![CocoaPods Compatible](https://img.shields.io/cocoapods/v/AoE.svg)](https://cocoapods.org/pods/AoE)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/didi/aoe/blob/master/LICENSE)

 [文档](https://didi.github.io/AoE/) |
 [发布记录](./CHANGELOG.md) |
 [路线图](./ROADMAP.md) |
 [English](./README_en.md)

## 一、简介

**AoE** (AI on Edge) 是一个开源的 **终端侧 AI 集成运行时环境 ( IRE )**。帮助开发者将不同框架的深度学习算法轻松部署到终端高效执行。

## 二、使用文档&示例&资源
- [Android用户指南](./Android/README.md)
- [iOS用户指南](./iOS/README.md)
- [Android Demo](./Android/examples/demo)
- [iOS Demo](./iOS/Demo)
- 官方模型：
    * [SeesawNet_pytorch](https://github.com/cvtower/seesawfacenet_pytorch) 人脸比对模型, https://arxiv.org/abs/1908.09124

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
        1.1.3 <br>
        （2020/01/16）
        </center>
        </td>
        <td>TensorFlow Lite</td>
        <td>
        [ Android ] com.didi.aoe.runtime-tensorflow-lite:1.1.3 <br>
        [   iOS   ] pod 'AoERuntime/TensorFlowLite','~> 1.1.3', <br>
        </td>
        <td>2.1.0</td>
    </tr>
    <tr>
        <td>PyTorch</td>
        <td>
        [ Android ] com.didi.aoe.runtime-pytorch:1.1.3
        </td>
        <td>1.4.0</td>
    </tr>
    <tr>
        <td>MNN</td>
        <td>
        [ Android ] com.didi.aoe.runtime-mnn:1.1.3 <br>
        [   iOS   ] pod 'AoERuntime/MNN','~> 1.1.3', <br>
        </td>
        <td>0.2.1.7</td>
    </tr>
    <tr>
        <td>NCNN</td>
        <td>
        [ Android ] com.didi.aoe.runtime-ncnn:1.1.3 <br>
        [   iOS   ] pod 'AoERuntime/NCNN','~> 1.1.3', <br>
        </td>
        <td>20200106</td>
    </tr>
</table>

[_更多版本适配信息_](https://didi.github.io/AoE/other/compat)

## 四、Q&A

* 加微信入群：*普惠出行产品技术*（备注 `AoE`）

    <img alt="普惠出行产品技术" src="https://img0.didiglobal.com/static/gstar/img/P8X9qN1d1B1576217775521.jpeg" width="196">
    

## 五、项目成员
### 核心成员

[kuloud](https://github.com/Kuloud)、
[dingc](https://github.com/qtdc1229) 、
[coleman.zou](https://github.com/zouyuefu) 、
[yangke1120](https://github.com/yangke1120) 、
[cvtower](https://github.com/cvtower) 

## 六、友情链接
* [Dokit](https://github.com/didi/DoraemonKit)，一款功能齐全的客户端（ iOS 、Android ）研发助手，你值得拥有 :)
* 普惠出行产品技术公众号，欢迎关注。

    <img alt="普惠出行产品技术公众号" src="https://img0.didiglobal.com/static/gstar/img/NlLuFeiqKU1570690897784.jpg" width="196">
