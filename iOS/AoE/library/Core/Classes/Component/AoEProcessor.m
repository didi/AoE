//
//  AoEProcessor.m
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import "AoEProcessor.h"
#import "AoEValidJudge.h"
#import "AoEClientOption.h"
#import "AoEComponentProvider.h"
#if __has_include("AoElogger.h")
#import "AoElogger.h"
#endif
@interface AoEProcessor ()

@property (strong , nonatomic) AoEClientOption *clientOption;
@property (strong , nonatomic) id<AoELoggerComponentProtocol> logger;
@end

@implementation AoEProcessor

- (instancetype)initWithClientOption:(AoEClientOption *)clientOption {
    self = [super init];
    if (self) {
        self.clientOption = clientOption;
        [self setupLoggerFromClass:clientOption.loggerClassName];
        AoEComponentProvider.logger = self.logger;
    }
    return self;
}

- (id<AoEInterpreterComponentProtocol>)getInterpreterComponent {
    [self.logger infoLog:@"getInterpreter"];
    [self.logger debugLog:[NSString stringWithFormat:@"%@",self.clientOption.interpreterClassName]];
    return [AoEComponentProvider getInterpreter:self.clientOption.interpreterClassName];
}

- (id<AoEModelLoaderComponentProtocol>)getModelLoaderComponent {
    return [AoEComponentProvider getModelLoader:self.clientOption.modelOptionLoaderClassName];
}

- (void)setupLoggerFromClass:(NSString *)loggerClassName {
    if ([AoEValidJudge isValidString:loggerClassName] && NSClassFromString(loggerClassName) &&
        [NSClassFromString(loggerClassName) instancesRespondToSelector:@selector(setLogTag:)]) {
        self.logger = [NSClassFromString(loggerClassName) new];
        [self.logger setLogTag:@"Processor"];
    }else {
#if __has_include("AoElogger.h")
        self.logger = [[AoElogger alloc] initWithTag:@"Processor"];
#endif
    }
}

@end
