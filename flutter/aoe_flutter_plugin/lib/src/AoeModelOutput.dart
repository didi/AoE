import 'package:aoeflutter/src/AoePerformance.dart';
import 'package:json_annotation/json_annotation.dart';

part 'AoeModelOutput.g.dart';

@JsonSerializable(createToJson: false, anyMap: true)
class AoeModelOutput {
  AoeModelOutput();

  dynamic data;
  Map<String, dynamic> error;
  AoePerformance performance;

  factory AoeModelOutput.fromJson(Map json) => _$AoeModelOutputFromJson(json);
}
