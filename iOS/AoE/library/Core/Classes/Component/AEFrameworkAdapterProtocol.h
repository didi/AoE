//
//  AEFrameworkAdapterProtocol.h
//  AoE
//
//  Created by dingchao on 2019/7/22.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

@protocol AEFrameworkAdapterProtocol <NSObject>

- (BOOL)isReady;

- (id)run:(id)input;

@optional
-(id<AoELoggerComponentProtocol>)loggerComponent;
@end
