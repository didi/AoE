// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'AoePerformanceItem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AoePerformanceItem _$AoePerformanceItemFromJson(Map json) {
  return AoePerformanceItem()
    ..max = (json['max'] as num)?.toDouble()
    ..avg = (json['avg'] as num)?.toDouble()
    ..min = (json['min'] as num)?.toDouble();
}

Map<String, dynamic> _$AoePerformanceItemToJson(AoePerformanceItem instance) =>
    <String, dynamic>{
      'max': instance.max,
      'avg': instance.avg,
      'min': instance.min
    };
