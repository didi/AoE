//
//  AoEUpgradeService.m
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import "AoEUpgradeService.h"
#import "AoEUpgradeModel.h"
#import "AoEVersionUtil.h"
#import "AoEFileManager.h"
#import "AoEUpgradeRequest.h"
#import "AoECheckUpgradeProtocol.h"

#import "AoEValidJudge.h"
#import "AoECryptoUtil.h"
#import <SSZipArchive/SSZipArchive.h>

static NSInteger const kUpgradeTimeInterval = 2*60*60; // 2h
static NSString *const AoEUpgradeLockObj = @"AoEUpgradeLockObj";
@interface AoEUpgradeService ()
@property (nonatomic, strong) dispatch_queue_t upgradQueue;
/** key:model name  value:AoEUpgradeModel instance */
@property (nonatomic, strong) NSMutableDictionary *upgradingModelDic;
@end

@implementation AoEUpgradeService

#pragma mark - interface

+ (AoEUpgradeService *)shareInstance {
    static AoEUpgradeService *instance = nil;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        instance = [[AoEUpgradeService alloc] init];
        [instance upgradingModelDic];
        instance.upgradQueue = dispatch_queue_create("AoEUpgradeServiceQueue", DISPATCH_QUEUE_SERIAL);
    });
    
    return instance;
}

- (void)dispatchUpgradeService:(AoEUpgradeServiceInputModel *)model {
    if (![self validateInputModel:model]) {
        return;
    }
    
    AoEUpgradeModel *upgradeModel = [self fetchUpgradingModelWithModelName:model.name];
    
    //1 正在获取最新版本，不再执行更新逻辑
    if (upgradeModel.upgrading) {
        return;
    }
    
    //2 2小时内已请求过最新版本，不再执行更新逻辑
    if ([[NSDate date] timeIntervalSinceDate:upgradeModel.lastUpgradeDate] <= kUpgradeTimeInterval) {
        return;
    }
    
    //3 本地有比verison大的版本，不再执行更新逻辑
    if ([self localModelHasGreaterVersionThanVersion:model.version modelName:model.alias path:model.storagePath]) {
        return;
    }
    
    // 标记开始更新
    [self markStartUpgrade:model.name];
    
    //4 获取服务端最新版本配置，检测是否有新版本，有则开启下载
    __weak typeof(self) weakSelf = self;
    [AoEUpgradeRequest requestUpgradePlistWithUrl:model.url params:model.requestParams successBlock:^(NSDictionary *response) {
        __strong typeof(weakSelf) strongifySelf = weakSelf;
        id <AoECheckUpgradeProtocol> downloadModel = [strongifySelf getDownloadModelWithRespone:response checkUpgradeClass:model.checkUpgradeModel];
        NSString *newerVersion = downloadModel.version;
        if (!newerVersion ||
            ![AoEVersionUtil isValidVersion:newerVersion] ||
            (!model.needDownloadImmediately &&
             ![AoEVersionUtil version:newerVersion isGreaterThanVersion:model.version])) {
            [strongifySelf markStopUpgrade:model.name success:NO];
        } else {
            [strongifySelf startDownloadTaskWithModel:model downloadModel:downloadModel];
        }
    } failureBlock:^(NSError *error) {
        __strong typeof(weakSelf) strongifySelf = weakSelf;
        [strongifySelf markStopUpgrade:model.name success:NO];
    }];
}

- (void)startUpgradeService:(AoEUpgradeServiceInputModel *)model {
    dispatch_async(self.upgradQueue, ^{
        [self dispatchUpgradeService:model];
    });
}

#pragma mark - private

- (void)markStartUpgrade:(NSString *)name {
    AoEUpgradeModel *upgradeModel = [self fetchUpgradingModelWithModelName:name];
    upgradeModel.upgrading = YES;
}

- (void)markStopUpgrade:(NSString *)name success:(BOOL)success {
    AoEUpgradeModel *upgradeModel = [self fetchUpgradingModelWithModelName:name];
    upgradeModel.upgrading = NO;
    if (success) {
        upgradeModel.lastUpgradeDate = [NSDate date];
    }
}

- (void)startDownloadTaskWithModel:(AoEUpgradeServiceInputModel *)model downloadModel:(id <AoECheckUpgradeProtocol>)downloadModel {
    __weak typeof(self) weakSelf = self;
    NSString *downUrl = downloadModel.downloadUrl;
    [AoEUpgradeRequest downloadWithUrlString:downUrl successBlock:^(NSString *cachePath) {
        __strong typeof(weakSelf) strongifySelf = weakSelf;
        NSNumber *zipSize = downloadModel.size;
        NSString *zipMd5 = downloadModel.sign;
        NSData *data = [NSData dataWithContentsOfFile:cachePath];
        BOOL needValidJudge = NO;
        if ((zipSize &&
            [zipSize isKindOfClass:[NSNumber class]]) ||
            [AoEValidJudge isValidString:zipMd5]) {
            needValidJudge = YES;
        }
        
        if (needValidJudge &&
            (zipSize.longLongValue == data.length ||
            [[AoECryptoUtil aoe_encryptMD5Data:data] isEqualToString:zipMd5])) {
            [strongifySelf storeDownloadDataWithModel:model downloadModel:downloadModel cachePath:cachePath];
        }else {
            [strongifySelf markStopUpgrade:model.name success:NO];
        }
    } failureBlock:^(NSError *error) {
        __strong typeof(weakSelf) strongifySelf = weakSelf;
        [strongifySelf markStopUpgrade:model.name success:NO];
    }];
}

- (void)storeDownloadDataWithModel:(AoEUpgradeServiceInputModel *)model downloadModel:(id <AoECheckUpgradeProtocol>)downloadModel cachePath:(NSString *)cachePath {
    
    //1 如果本地已有>=2个版本，只保留其中最新的版本
    //    NSString *modelPath = [model.storagePath stringByAppendingPathComponent:model.name];
    NSArray *localVersions = [AoEFileManager fetchFilesWithPath:model.storagePath];
    if (localVersions.count >= 2) {
        NSString *localLastVersion = [AoEVersionUtil findLastVersionFromVersions:localVersions];
        NSMutableArray *mutVersions = [NSMutableArray arrayWithArray:localVersions];
        [mutVersions removeObject:localLastVersion];
        for (NSString *item in mutVersions) {
            NSString *deletePath = [NSString stringWithFormat:@"%@/%@", model.storagePath, item];
            if (![AoEFileManager deleteFileWithPath:deletePath]) {
                [self markStopUpgrade:model.name success:NO];
                return;
            }
        }
    }
    
    //2 存储下载的文件
    NSDictionary *fileDic = [[NSFileManager defaultManager] attributesOfItemAtPath:cachePath error:nil];
    CGFloat zipSize = [[fileDic objectForKey:NSFileSize] longLongValue] / 1024.0;
    CGFloat freeSize = [AoEFileManager freeSizeInDocumentDirectory];
    NSString *savePath = [NSString stringWithFormat:@"%@/%@", model.storagePath, downloadModel.version];
    
    if (zipSize > 0 && freeSize > 0 && freeSize > 2*zipSize) {
        NSError *error = nil;
        BOOL unzipState = [SSZipArchive unzipFileAtPath:cachePath toDestination:savePath overwrite:YES password:nil error:&error];
        if (unzipState) {
            [self markStopUpgrade:model.name success:YES];
        } else {
            [self markStopUpgrade:model.name success:NO];
            [AoEFileManager deleteFileWithPath:savePath];
        }
    } else {
        [self markStopUpgrade:model.name success:NO];
    }
}

#pragma mark -

- (NSMutableDictionary *)upgradingModelDic {
    if (!_upgradingModelDic) {
        _upgradingModelDic = [NSMutableDictionary dictionary];
    }
    return _upgradingModelDic;
}

- (id<AoECheckUpgradeProtocol>)getDownloadModelWithRespone:(NSDictionary *)respone checkUpgradeClass:(NSString *)className {
    if (![NSClassFromString(className) respondsToSelector:@selector(instanceWithDictionary:)]) {
        return nil;
    }
    return [NSClassFromString(className) instanceWithDictionary:respone];
}

#pragma mark - utility

- (BOOL)validateInputModel:(AoEUpgradeServiceInputModel *)model {
    if (![AoEValidJudge isValidString:model.name] ||
        ![AoEValidJudge isValidString:model.url] ||
        ![AoEValidJudge isValidString:model.storagePath]) {
        return NO;
    }
    
    if (![AoEVersionUtil isValidVersion:model.version]) {
        return NO;
    }
    
    return YES;
}

- (AoEUpgradeModel *)fetchUpgradingModelWithModelName:(NSString *)name {
    AoEUpgradeModel *upgradeModel = self.upgradingModelDic[name];
    if (!upgradeModel) {
        upgradeModel = [[AoEUpgradeModel alloc] init];
        upgradeModel.name = name;
        upgradeModel.upgrading = NO;
        upgradeModel.lastUpgradeDate = [NSDate dateWithTimeIntervalSince1970:0];
        @synchronized (AoEUpgradeLockObj) {
            [self.upgradingModelDic setObject:upgradeModel forKey:name];
        }
    }
    return upgradeModel;
}

- (BOOL)localModelHasGreaterVersionThanVersion:(NSString *)version modelName:(NSString *)name path:(NSString *)path {
    NSArray *versionArray = [AoEFileManager fetchFilesWithPath:path];
    
    // error
    if (!versionArray) {
        return YES;
    }
    
    if (versionArray.count == 0) {
        return NO;
    }
    
    for (NSString *subVersion in versionArray) {
        if (![AoEVersionUtil isValidVersion:subVersion]) {
            return YES;
        }
    }
    
    for (NSString *subVersion in versionArray) {
        if ([subVersion isEqualToString:version]) {
            return YES;
        }
        if ([AoEVersionUtil version:subVersion isGreaterThanVersion:version]) {
            return YES;
        }
    }
    
    return NO;
}

@end
