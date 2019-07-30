//
//  AoEComponentProvider.h
//  AoE
//
//  Created by dingchao on 2019/7/2.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

@interface AoEComponentProvider : NSObject
@property(nonatomic , class ,strong) id<AoELoggerComponentProtocol> logger;

+ (id<AoEInterpreterComponentProtocol>)getInterpreter:(NSString *)className;
+ (id<AoEModelLoaderComponentProtocol>)getModelLoader:(NSString *)className;
@end

