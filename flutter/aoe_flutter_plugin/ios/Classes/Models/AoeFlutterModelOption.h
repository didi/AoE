//
//  AoeFlutterModelOption.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import "AoEModelOption.h"
#import "AoeCommonInterceptor.h"
#import <AoE/AoEModelOption.h>

@interface AoeFlutterModelOption : AoEModelOption

@property (nonatomic ,strong) NSString *modelFileName;

@property (nonatomic ,strong) NSString *modelParamFileName;

@end

