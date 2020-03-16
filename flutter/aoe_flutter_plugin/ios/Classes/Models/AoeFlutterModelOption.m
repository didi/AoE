//
//  AoeFlutterModelOption.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import "AoeFlutterModelOption.h"

@implementation AoeFlutterModelOption

- (instancetype)initWithDictionary:(NSDictionary *)opt {
    self = [super initWithDictionary:opt];
    if (self) {
        self.modelFileName = opt[@"modelFileName"];
        self.modelParamFileName = opt[@"modelParamFileName"];
    }
    return self;
}

@end
