//
//  AoEClientOption.h
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import <Foundation/Foundation.h>

@interface AoEClientOption : NSObject

/**
 处理流的className
 */
@property (strong, nonatomic) NSString *processorClassName;
/**
 模型加载器的className
 */
@property (strong, nonatomic) NSString *modelOptionLoaderClassName;
/**
 模型处理器的className
 */
@property (strong, nonatomic) NSString *interpreterClassName;
/**
 日志模块加载的className
 */
@property (strong, nonatomic) NSString *loggerClassName;
/**
 为升级模块提供纬度
 */
@property (strong, nonatomic) NSString *lat;
/**
 为升级模块提供经度
 */
@property (strong, nonatomic) NSString *lng;
/**
 为升级模块提供经度
 */
@property (strong, nonatomic) NSString *appKey;
/**
 升级模块需要使用appId
*/
@property (assign, nonatomic) NSInteger appId;

- (instancetype)setModelOptionLoader:(Class)modelOptionLoader;
@end
