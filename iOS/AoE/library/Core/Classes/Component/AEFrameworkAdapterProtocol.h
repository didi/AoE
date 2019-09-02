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

- (NSArray <id> *)run:(NSArray <id> *)input;

@optional
-(id<AoELoggerComponentProtocol>)loggerComponent;
@end

@protocol AEFrameworkAdapterTensorProtocol <NSObject>

@property (nonatomic , assign) NSInteger index;
@property (nonatomic ,strong) NSValue *tensorData;

//@property (nonatomic ,strong) NSArray *sharps;
@end

