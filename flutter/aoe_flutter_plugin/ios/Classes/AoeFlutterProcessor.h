//
//  AoeFlutterProcessor.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import "AoEProcessor.h"
#import <AoE/AoEClientOption.h>
#import "AoeCommonInterceptor.h"

@interface AoEClientOption (Flutter)
@property (nonatomic, assign) AoeInterceptorType runtimeType;
@end

@interface AoeFlutterProcessor : AoEProcessor
@end
