//
//  AoEFileManager.m
//  AoE
//
//  Created by dingchao on 2019/3/26.
//

#import "AoEFileManager.h"

@implementation AoEFileManager

+ (NSArray *)fetchFilesWithPath:(NSString *)path {
    NSFileManager *manager = [NSFileManager defaultManager];
    BOOL isDir = NO;
    BOOL isExist = [manager fileExistsAtPath:path isDirectory:&isDir];
    
    if (isExist && isDir) {
        NSError *error = nil;
        NSArray *subFiles = [manager contentsOfDirectoryAtPath:path error:&error];
        if (!error) {
            return subFiles ? subFiles : @[];
        }
    } else {
        return @[];
    }
    
    return nil;
}

+ (BOOL)deleteFileWithPath:(NSString *)path {
    NSFileManager *manager = [NSFileManager defaultManager];
    if (![manager fileExistsAtPath:path]) {
        return NO;
    }
    
    NSError *error = nil;
    return [manager removeItemAtPath:path error:&error];
}

+ (CGFloat)freeSizeInDocumentDirectory {
    CGFloat freesize = 0.0;
    NSError *error = nil;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSDictionary *dictionary = [[NSFileManager defaultManager] attributesOfFileSystemForPath:[paths lastObject] error: &error];
    if (dictionary) {
        NSNumber *freeNum = dictionary[NSFileSystemFreeSize];
        freesize = [freeNum unsignedLongLongValue]*1.0/(1024);
        return freesize;
    }
    
    return 0.0;
}

@end
