import 'package:aoeflutter/aoeflutter.dart';
import 'package:json_annotation/json_annotation.dart';
import "AoePerformanceItem.dart";

part 'AoePerformance.g.dart';

@JsonSerializable(explicitToJson: true, anyMap: true)
class AoePerformance {
  AoePerformance();

  AoePerformanceItem cpu;
  @JsonKey(name: 'mem')
  AoePerformanceItem memory;
  AoePerformanceItem time;
  AoeDeviceInfo device;

  factory AoePerformance.fromJson(Map json) => _$AoePerformanceFromJson(json);

  Map<String, dynamic> toJson() => _$AoePerformanceToJson(this);
}
