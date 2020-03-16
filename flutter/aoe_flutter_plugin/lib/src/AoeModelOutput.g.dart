// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'AoeModelOutput.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AoeModelOutput _$AoeModelOutputFromJson(Map json) {
  return AoeModelOutput()
    ..data = json['data']
    ..error = (json['error'] as Map)?.map(
      (k, e) => MapEntry(k as String, e),
    )
    ..performance = json['performance'] == null
        ? null
        : AoePerformance.fromJson(json['performance'] as Map);
}
