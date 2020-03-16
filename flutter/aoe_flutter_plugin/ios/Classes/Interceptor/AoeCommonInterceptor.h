//
//  AoeCommonInterceptor.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/11.
//

#import <Foundation/Foundation.h>
#import <Aoe/AoEProcessorProtocol.h>

//支持的推理框架类型
typedef NS_ENUM(NSUInteger, AoeInterceptorType) {
    AoeInterceptorTypeUnkonw,
    AoeInterceptorTypeTFlow,
    AoeInterceptorTypeNCNN,
    AoeInterceptorTypeMNN
};

@interface AoeCommonInterceptor : NSObject<AoEInterpreterComponentProtocol>

@property (nonatomic, assign) AoeInterceptorType type;

@end
