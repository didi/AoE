//
//  AoEModelOptionProtocol.h
//  AoE
//
//  Created by dingchao on 2019/3/20.
//

#import <Foundation/Foundation.h>

/**
 * 模型配置参数
 *
 * @author dingc
 * @date 2019/3/20
 */
@protocol AoEModelOptionProtocol <NSObject>

- (NSString *)modelDirPath;
- (NSString *)modelName;
- (BOOL)isValidOption;

@end
