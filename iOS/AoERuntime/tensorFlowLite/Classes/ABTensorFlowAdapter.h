//
//  ABTensorFlowAdapter.h
//  AoEBiz
//
//  Created by dingchao on 2019/7/22.
//

#import <Foundation/Foundation.h>
#import <AoE/AEFrameworkAdapterProtocol.h>

@interface ABTensorFlowAdapter : NSObject <AEFrameworkAdapterProtocol>

- (instancetype)initWithPath:(NSString *)path;
@end

