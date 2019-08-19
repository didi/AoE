//
//  AoECryptoManager.h
//  AoE
//
//  Created by dingchao on 2019/8/19.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

@interface AoECryptoManager : NSObject <AoECryptoComponentProtocol>

@property (nonatomic ,strong)NSString *encodeKey;
@property (nonatomic ,strong)NSData *iv;
@end

