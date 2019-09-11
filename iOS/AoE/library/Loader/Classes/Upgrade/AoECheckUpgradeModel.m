//
//  AoECheckUpgradeModel.m
//  AoE
//
//  Created by dingchao on 2019/9/11.
//

#import "AoECheckUpgradeModel.h"
#import "AoEValidJudge.h"

@interface AoECheckUpgradeModel ()
@property (nonatomic ,strong) NSDictionary *dic;
@end

@implementation AoECheckUpgradeModel

+ (instancetype)instanceWithDictionary:(NSDictionary *)dictionary {
    if (![AoEValidJudge isValidDictionary:dictionary]) {
        return nil;
    }
    
    NSString *modelKey = dictionary.allKeys.firstObject;
    NSDictionary *modelDic = dictionary[modelKey];
    AoECheckUpgradeModel *model = [AoECheckUpgradeModel new];
    model.dic = modelDic;
    return model;
}
- (NSString *)version {
    return self.dic[@"version"];
}
- (NSString *)downloadUrl {
    return self.dic[@"upgradeUrl"];
}
- (NSNumber *)size {
    return self.dic[@"size"];
}
- (NSString *)sign {
    return self.dic[@"sign"];
}

@end
