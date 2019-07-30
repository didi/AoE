//
//  AoEClientOption.m
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import "AoEClientOption.h"

@implementation AoEClientOption

- (AoEClientOption *)setModelOptionLoader:(Class)modelOptionLoader {
    self.modelOptionLoaderClassName = NSStringFromClass(modelOptionLoader);
    return self;
}

- (NSString *)description {
    NSString *des = [NSString stringWithFormat:@"Loader:[%@],processor:[%@],interpreter:[%@],logger:[%@]",
                     self.modelOptionLoaderClassName,
                     self.processorClassName,
                     self.interpreterClassName,
                     self.loggerClassName];
    return des;
}

@end
