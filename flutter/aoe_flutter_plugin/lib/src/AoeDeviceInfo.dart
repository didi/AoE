import 'package:json_annotation/json_annotation.dart';

part 'AoeDeviceInfo.g.dart';

@JsonSerializable(explicitToJson: true, anyMap: true)
class AoeDeviceInfo {
  AoeDeviceInfo();

  String uuid;
  String name;
  String model;
  String system;
  String version;
  String cpu;
  String disk;
  String memory;
  String ip;
  String macAddress;
  Map<String, dynamic> extInfo;

  factory AoeDeviceInfo.fromJson(Map json) => _$AoeDeviceInfoFromJson(json);

  Map<String, dynamic> toJson() => _$AoeDeviceInfoToJson(this);
}
