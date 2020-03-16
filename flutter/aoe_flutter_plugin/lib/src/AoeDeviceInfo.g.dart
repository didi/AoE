// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'AoeDeviceInfo.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AoeDeviceInfo _$AoeDeviceInfoFromJson(Map json) {
  return AoeDeviceInfo()
    ..uuid = json['uuid'] as String
    ..name = json['name'] as String
    ..model = json['model'] as String
    ..system = json['system'] as String
    ..version = json['version'] as String
    ..cpu = json['cpu'] as String
    ..disk = json['disk'] as String
    ..memory = json['memory'] as String
    ..ip = json['ip'] as String
    ..macAddress = json['macAddress'] as String
    ..extInfo = (json['extInfo'] as Map)?.map(
      (k, e) => MapEntry(k as String, e),
    );
}

Map<String, dynamic> _$AoeDeviceInfoToJson(AoeDeviceInfo instance) =>
    <String, dynamic>{
      'uuid': instance.uuid,
      'name': instance.name,
      'model': instance.model,
      'system': instance.system,
      'version': instance.version,
      'cpu': instance.cpu,
      'disk': instance.disk,
      'memory': instance.memory,
      'ip': instance.ip,
      'macAddress': instance.macAddress,
      'extInfo': instance.extInfo
    };
