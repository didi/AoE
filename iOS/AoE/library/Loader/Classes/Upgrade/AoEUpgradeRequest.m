//
//  AoEUpgradeRequest.m
//  AoE
//
//  Created by dingchao on 2019/9/10.
//

#import "AoEUpgradeRequest.h"

@implementation AoEUpgradeRequest

+ (void)requestUpgradePlistWithUrl:(NSString *)urlString
                            params:(NSDictionary *)params
                      successBlock:(AoEUpgradeReqSucceedBlock)successBlock
                      failureBlock:(AoEUpgradeReqFailureBlock)failureBlock {
    NSURL *url = [NSURL URLWithString:urlString];
    if (!url) {
        NSError *error = [NSError errorWithDomain:@""
                                             code:-1
                                         userInfo:@{@"msg":[NSString stringWithFormat:@"url非法[%@]", urlString]}];
        if (failureBlock) failureBlock(error);
        return;
    }

    NSMutableURLRequest *request =[NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    NSError *error = nil;
    request.HTTPBody = [NSJSONSerialization dataWithJSONObject:params   options:NSJSONWritingPrettyPrinted error:&error];
    if (error) {
        if (failureBlock) failureBlock(error);
        return;
    }
    NSURLSessionDataTask *dataTask = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            NSDictionary *responseDic = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:nil];
            NSDictionary *responseData = responseDic[@"data"];
            if (successBlock) successBlock(responseData);
        } else {
            if (failureBlock) failureBlock(error);
        }
    }];
    
    [dataTask resume];
}

+ (void)downloadWithUrlString:(NSString *)urlString
                 successBlock:(void(^)(NSString *cachePath))successBlock
                 failureBlock:(void(^)(NSError *error))failureBlock {
    if (!urlString || ![urlString isKindOfClass:[NSString class]] || urlString.length == 0) {
        return;
    }
    
    NSURL *url = [NSURL URLWithString:urlString];
    NSURLSession *session = [NSURLSession sharedSession];
    
    NSURLSessionDownloadTask *downloadTask = [session downloadTaskWithURL:url completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            successBlock(location.path);
        } else {
            failureBlock(error);
        }
    }];
    
    [downloadTask resume];
}

@end
