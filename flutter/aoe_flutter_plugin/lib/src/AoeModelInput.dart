import 'dart:typed_data';
import 'package:json_annotation/json_annotation.dart';

part 'AoeModelInput.g.dart';

///Aoe运行上下文信息
@JsonSerializable(createFactory: false)
class AoeModelInput {
  ///推理框架使用 模型提供
  String inBlobKey;

  ///推理框架使用 模型提供
  String outBlobKey;

  ///图片源格式 取决于样本数据
  num sourceFormat;

  ///图片目标格式 取决于模型
  num targetFormat;

  ///源图片尺寸
  Map<String, num> sourceSize;

  ///目标图片尺寸 取决于模型
  Map<String, num> blockSize;

  ///模型数据处理插值参数
  Uint8List meanVals;

  ///模型数据处理插值参数
  Uint8List normVals;

  ///图片数据
  Uint8List data;

  AoeModelInput(
      this.inBlobKey,
      this.outBlobKey,
      this.sourceFormat,
      this.targetFormat,
      this.sourceSize,
      this.blockSize,
      this.meanVals,
      this.normVals,
      this.data);

  Map<String, dynamic> toJson() => _$AoeModelInputToJson(this);
}
