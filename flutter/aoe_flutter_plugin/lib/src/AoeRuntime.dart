import 'dart:async';
import 'package:aoeflutter/aoeflutter.dart';
import 'package:aoeflutter/src/AoeModelOutput.dart';
import 'package:flutter/services.dart';
import 'AoeModelInput.dart';

///Aoe运行上下文信息
class AoeRuntime {
  ///推理框架类型 0 未知 1 TF 2 NCNN 3 MNN
  int runtime;

  ///模型所在目录 用于加载模型文件
  String modelDir;

  ///是否准备好
  bool isReady = false;

  static MethodChannel _channel = const MethodChannel('aoe_flutter');

  AoeRuntime(this.runtime, this.modelDir);

  ///初始化推理上下文
  Future<AoeModelOutput> start() async {
    AoeModelOutput result = AoeModelOutput.fromJson(
        await _channel.invokeMethod('start', this.context));
    isReady = result.error == null || result.error.length == 0;
    return result;
  }

  ///执行推理
  Future<AoeModelOutput> process(AoeModelInput modelInput) async {
    if (!isReady) {
      return null;
    }
    AoeModelOutput output = AoeModelOutput.fromJson(
        await _channel.invokeMethod('process', modelInput?.toJson()));
    if ((output.error == null || output.error.length == 0) &&
        output.data is List) {
      output.data = (output.data as List)
//          .expand((pair) => pair)
          .map((f) => (f as int))
          .toList();
    }
    return output;
  }

  ///结束推理
  Future<bool> stop() async {
    if (!isReady) {
      return true;
    }
    return await _channel.invokeMethod('stop');
  }

  ///上下文信息
  Map<String, dynamic> get context =>
      {'runtime': this?.runtime, 'modelDir': this?.modelDir};
}
