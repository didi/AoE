//
//  AoECryptoManager.m
//  AoE
//
//  Created by dingchao on 2019/8/19.
//

#import "AoECryptoManager.h"
#import "AoEModelOption.h"
#import "AoECryptoUtil.h"

@implementation AoECryptoManager

- (NSData *)decryptModel:(NSData *)modelsData option:(id<AoEModelOptionProtocol>)option {
    return [AoECryptoUtil aoe_decryptAoEModel:modelsData
                                   encryptKey:self.encodeKey
                                       offset:self.iv
                                     saltType:AoECryptoTypeForAES128];
}
@end
