//
//  AoeTensorFlowCommonInterceptor.m
//  aoe_flutter
//
//  Created by UJOY on 2019/10/12.
//

#import "AoeTensorFlowCommonInterceptor.h"
#import "AoeFlutterModelOption.h"
#import "AoeFlutterInputModel.h"
#import "ARTensorFlowAdapter.h"
#import <AoE/AoElogger.h>
#import <AoE/AoECryptoManager.h>
#import <AoE/AoEValidJudge.h>
#import <AoE/AoECryptoUtil.h>

@interface AoeTensorFlowCommonInterceptor ()
@property(nonatomic ,strong) ARTensorFlowAdapter *adapter;
@property(nonatomic ,strong) AoElogger *logger;
@property(nonatomic ,strong) NSString *decodeModelPath;
@end

@implementation AoeTensorFlowCommonInterceptor

- (void)close {
    self.adapter = nil;
    if (self.decodeModelPath &&
        [[NSFileManager defaultManager] fileExistsAtPath:self.decodeModelPath]) {
        [[NSFileManager defaultManager] removeItemAtPath:self.decodeModelPath error:nil];
    }
}

- (BOOL)isReady {
    return self.adapter != nil;
}

- (id<AoELoggerComponentProtocol>)loggerComponent {
    return self.logger;
}

- (id<AoEOutputModelProtocol> )run:(id<AoEInputModelProtocol>)input {
    
    if (!self.isReady) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"TFLInterpreter instance is not ready"]];
        return nil;
    }
    
    AoeFlutterInputModel * inputModel = (AoeFlutterInputModel *)input;
    NSArray *outDatas = nil;
    if (self.adapter &&
        [self.adapter respondsToSelector:@selector(run:)]) {
        outDatas = [self.adapter run:@[inputModel.data]];
    }
    
    if (!outDatas ||
        outDatas.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"get outData error "]];
        return nil;
    }
    
    return (id<AoEOutputModelProtocol>)outDatas;
    
}

- (BOOL)setupModel:(NSArray<id<AoEModelOptionProtocol>> *)options {
    if (options.count < 1) {
        [[self loggerComponent] errorLog:[NSString stringWithFormat:@"not have enough options"]];
        return NO;
    }
    AoeFlutterModelOption *option = (AoeFlutterModelOption  *) options.firstObject;
    NSString *modelPath = [option.modelDirPath stringByAppendingPathComponent:option.modelFileName];
    modelPath = [self decodeModelWithPath:modelPath option:option];
    self.adapter = [[ARTensorFlowAdapter alloc] initWithPath:modelPath];
    return self.adapter != nil;
}

# pragma mark - decode model

- (NSString *)decodeModelWithPath:(NSString *)modelPath option:(AoeFlutterModelOption *)option {
    NSString *decodeModelPath = modelPath;
    if (option.encrypted) {
        if ([AoEValidJudge isValidString:self.decodeModelPath]) {
            return self.decodeModelPath;
        }
        AoECryptoManager *cryptoManager = [AoECryptoManager new];
        NSData *source = [[NSData alloc] initWithContentsOfFile:modelPath];
        NSData *decodeData = [cryptoManager decryptModel:source option:option];
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSString *homePath = [[paths firstObject] stringByAppendingPathComponent:[AoECryptoUtil aoe_encryptMD5Data:[@"decode" dataUsingEncoding:NSUTF8StringEncoding]]];
        BOOL isDirectory = NO;
        if (![[NSFileManager defaultManager] fileExistsAtPath:homePath isDirectory:&isDirectory] ||
            !isDirectory) {
            [[NSFileManager defaultManager] createDirectoryAtPath:homePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        NSString *decodeModelName = [AoECryptoUtil aoe_encryptMD5Data:[modelPath.lastPathComponent dataUsingEncoding:NSUTF8StringEncoding]];
        decodeModelPath = [homePath stringByAppendingPathComponent:[decodeModelName stringByAppendingPathExtension:option.modelFileName.pathExtension]];
        [[NSFileManager defaultManager] createFileAtPath:decodeModelPath contents:nil attributes:nil];
        [decodeData writeToFile:decodeModelPath atomically:YES];
        self.decodeModelPath = decodeModelPath;
    }
    return decodeModelPath;
}

#pragma getter and setter

- (AoElogger *)logger {
    if (!_logger) {
        _logger = [[AoElogger alloc] initWithTag:NSStringFromClass([self class])];
    }
    return _logger;
}

@end
