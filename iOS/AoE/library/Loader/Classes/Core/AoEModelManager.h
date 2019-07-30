//
//  AoEModelManager
//  AoE
//
//  Created by dingchao on 2019/3/22.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

@interface AoEModelManager : NSObject <AoEModelLoaderComponentProtocol>

@property (nonatomic , strong) NSString *modelLoaderConfigClassName;
@property (nonatomic , strong) NSString *modelOptionClassName;
@end
