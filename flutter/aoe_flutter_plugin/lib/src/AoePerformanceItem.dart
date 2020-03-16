import 'package:json_annotation/json_annotation.dart';

part 'AoePerformanceItem.g.dart';

@JsonSerializable(anyMap: true)
class AoePerformanceItem {
  AoePerformanceItem();

  double max;
  double avg;
  double min;

  factory AoePerformanceItem.fromJson(Map json) =>
      _$AoePerformanceItemFromJson(json);

  Map<String, dynamic> toJson() => _$AoePerformanceItemToJson(this);
}
