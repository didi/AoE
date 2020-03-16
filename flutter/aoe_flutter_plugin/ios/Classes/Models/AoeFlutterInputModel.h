//
//  AoeFlutterInputModel.h
//  aoe_flutter
//
//  Created by UJOY on 2019/10/14.
//

#import <Foundation/Foundation.h>
#import <AoE/AoEInputModelProtocol.h>
#import <Flutter/Flutter.h>


@interface AoeFlutterInputModel : NSObject<AoEInputModelProtocol>

@property (nonatomic ,copy) NSString *inBlobKey;
@property (nonatomic ,copy) NSString *outBlobKey;
@property (nonatomic ,assign) NSInteger sourceFormat;
@property (nonatomic ,assign) NSInteger targetFormat;
@property (nonatomic ,assign) CGSize blockSize;
@property (nonatomic ,assign) CGSize sourceSize;

@property (nonatomic, strong) NSData * meanVals;
@property (nonatomic, strong) NSData * normVals;
@property (nonatomic, strong) NSData * data;

- (instancetype) initWithDict:(NSDictionary *)dict ;

- (BOOL)isValid;

@end
