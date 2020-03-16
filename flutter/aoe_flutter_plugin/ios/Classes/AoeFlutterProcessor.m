//
//  AoeFlutterProcessor.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import "AoeFlutterProcessor.h"
#import <objc/runtime.h>

@implementation AoEClientOption (Flutter)

- (AoeInterceptorType)runtimeType {
    return [objc_getAssociatedObject(self, @selector(runtimeType)) integerValue];
}

- (void) setRuntimeType:(AoeInterceptorType)runtimeType {
    objc_setAssociatedObject(self, @selector(runtimeType), @(runtimeType), OBJC_ASSOCIATION_COPY);
}
     
@end

@implementation AoeFlutterProcessor

- (id<AoEInterpreterComponentProtocol>)getInterpreterComponent {
    AoeCommonInterceptor * interpreter = [super getInterpreterComponent];
    AoEClientOption * option = [self valueForKey:@"clientOption"];
    interpreter.type = option.runtimeType;
    return interpreter;
}

@end
