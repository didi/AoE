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
    
    AoECheckUpgradeModel *model = [AoECheckUpgradeModel new];
    model.dic = dictionary;
    return model;
}
- (NSString *)version {
    return self.dic[@"modelVersionCode"];
}
- (NSString *)downloadUrl {
    return self.dic[@"modelUrl"];
}
- (NSNumber *)size {
    return self.dic[@"size"];
}

- (NSString *)sign {
    return self.dic[@"modelMd5"];
}

@end
