//
//  ABSqueezeModelOption.m
//  AoEBiz
//
//  Created by dingchao on 2019/7/9.
//

#import "ABSqueezeModelOption.h"

@implementation ABSqueezeModelOption

- (instancetype)initWithDictionary:(NSDictionary *)opt {
    self = [super initWithDictionary:opt];
    if (self) {
        self.modelFileName = opt[@"modelFileName"];
        self.modelParamFileName = opt[@"modelParamFileName"];
    }
    return self;
}

@end
