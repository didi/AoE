//
//  AoeFlutterInputModel.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import "AoeFlutterInputModel.h"
#import <Flutter/Flutter.h>

static inline  CGSize __ConvertToCGSize(NSDictionary * dict) {
    if (!dict) {
        return CGSizeZero;
    }
    CGFloat width = [dict[@"w"] floatValue];
    CGFloat height = [dict[@"h"] floatValue];
    return CGSizeMake(width, height);
}

@implementation AoeFlutterInputModel

- (instancetype) initWithDict:(NSDictionary *)dict {
    if (self = [super init]) {
        self.inBlobKey = dict[@"inBlobKey"];
        self.outBlobKey = dict[@"outBlobKey"];
        self.sourceFormat = [dict[@"sourceFormat"] integerValue];
        self.targetFormat = [dict[@"targetFormat"] integerValue];
        self.blockSize = __ConvertToCGSize(dict[@"blockSize"]);
        self.sourceSize = __ConvertToCGSize(dict[@"sourceSize"]);
        if(dict[@"meanVals"]){
            self.meanVals = ((FlutterStandardTypedData *)dict[@"meanVals"]).data;
        }
        if(dict[@"normVals"]){
            self.normVals = ((FlutterStandardTypedData *)dict[@"normVals"]).data;
        }
        if(dict[@"data"]){
            self.data = ((FlutterStandardTypedData *)dict[@"data"]).data;
        }
    }
    return self;
}

- (BOOL)isValid {
    return self.data && !CGSizeEqualToSize(self.sourceSize, CGSizeZero) && !CGSizeEqualToSize(self.blockSize, CGSizeZero);
}

@end
