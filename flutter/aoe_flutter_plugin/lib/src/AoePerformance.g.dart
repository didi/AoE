// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'AoePerformance.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AoePerformance _$AoePerformanceFromJson(Map json) {
  return AoePerformance()
    ..cpu = json['cpu'] == null
        ? null
        : AoePerformanceItem.fromJson(json['cpu'] as Map)
    ..memory = json['mem'] == null
        ? null
        : AoePerformanceItem.fromJson(json['mem'] as Map)
    ..time = json['time'] == null
        ? null
        : AoePerformanceItem.fromJson(json['time'] as Map)
    ..device = json['device'] == null
        ? null
        : AoeDeviceInfo.fromJson(json['device'] as Map);
}

Map<String, dynamic> _$AoePerformanceToJson(AoePerformance instance) =>
    <String, dynamic>{
      'cpu': instance.cpu?.toJson(),
      'mem': instance.memory?.toJson(),
      'time': instance.time?.toJson(),
      'device': instance.device?.toJson()
    };
