//
//  AoeCommonInterceptor.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/11.
//

#import "AoeCommonInterceptor.h"
#import "AoeTensorFlowCommonInterceptor.h"
#import "AoeMNNComonInterceptor.h"
#import "AoeNCNNCommonInterceptor.h"
#import <Flutter/Flutter.h>

@interface AoeCommonInterceptor ()
@property (nonatomic, strong) id<AoEInterpreterComponentProtocol> innerInterceptor;
@end

@implementation AoeCommonInterceptor

- (void)close {
    [self.innerInterceptor close];
}

- (id<AoEOutputModelProtocol>)run:(id<AoEInputModelProtocol>)input {
    return [self.innerInterceptor run:input];
}

- (BOOL)setupModel:(NSArray<id<AoEModelOptionProtocol>> *)options {
    return [self.innerInterceptor setupModel:options];
}

- (BOOL)isReady {
    return [self.innerInterceptor isReady];
}

- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.innerInterceptor.loggerComponent;
}

#pragma mark Getter

- (void)setType:(AoeInterceptorType)type {
    if (_type!=type) {
        [self.innerInterceptor close];
        self.innerInterceptor = nil;
    }
    _type = type;
}

- (id<AoEInterpreterComponentProtocol> )innerInterceptor {
    if (!_innerInterceptor) {
        switch (self.type) {
            case AoeInterceptorTypeTFlow:
                _innerInterceptor = [AoeTensorFlowCommonInterceptor new];
                break;
            case AoeInterceptorTypeMNN:
                _innerInterceptor = [AoeMNNComonInterceptor new];
            case AoeInterceptorTypeNCNN:
                _innerInterceptor = [AoeNCNNCommonInterceptor new];
            case AoeInterceptorTypeUnkonw:
                break;
        }
    }
    return _innerInterceptor;
}

@end
