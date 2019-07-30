//
//  AoEFoileManager.h
//  AoE
//
//  Created by dingchao on 2019/3/26.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>

@interface AoEFileManager : NSObject

+ (NSArray *)fetchFilesWithPath:(NSString *)path;

+ (BOOL)deleteFileWithPath:(NSString *)path;

/** 磁盘剩余空间，单位K */
+ (CGFloat)freeSizeInDocumentDirectory;

@end
