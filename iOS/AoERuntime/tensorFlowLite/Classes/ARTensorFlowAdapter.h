//
//  ARTensorFlowAdapter.h
//  AoEBiz
//
//  Created by dingchao on 2019/7/22.
//

#import <Foundation/Foundation.h>
#import <AoE/AoEFrameworkAdapterProtocol.h>

@interface ARTensorFlowAdapter : NSObject <AoEFrameworkAdapterProtocol>

- (instancetype)initWithPath:(NSString *)path;
@end

