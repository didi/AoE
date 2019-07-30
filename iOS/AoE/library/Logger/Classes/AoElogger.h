//
//  AoElogger.h
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import <Foundation/Foundation.h>
#import "AoEProcessorProtocol.h"

typedef NS_ENUM(NSUInteger, AoElogLevelType) {
    AoElogLevelTypeForError = 0,
    AoElogLevelTypeForWarning,
    AoElogLevelTypeForDebug,
    AoElogLevelTypeForInfo,
};

FOUNDATION_EXPORT void AoElogMessageFunc(NSInteger level,
                                         const char *file,
                                         int lineNumber,
                                         const char *functionName,
                                         NSString *format, ...) NS_FORMAT_FUNCTION(5,6);
#define LogVerbose(level, frmt, ...) \
do { AoElogMessageFunc(AoElogLevelTypeForDebug, __FILE__ ,__LINE__, __FUNCTION__, frmt, ##__VA_ARGS__);} while(0)
#define LogError(level, frmt, ...) \
do { AoElogMessageFunc(AoElogLevelTypeForError, __FILE__ ,__LINE__, __FUNCTION__, frmt, ##__VA_ARGS__);} while(0)
#define LogWarning(level, frmt, ...) \
do { AoElogMessageFunc(AoElogLevelTypeForWarning, __FILE__ ,__LINE__, __FUNCTION__, frmt, ##__VA_ARGS__);} while(0)
#define LogInfo(level, frmt, ...) \
do { AoElogMessageFunc(AoElogLevelTypeForInfo, __FILE__ ,__LINE__, __FUNCTION__, frmt, ##__VA_ARGS__);} while(0)



@interface AoElogger : NSObject <AoELoggerComponentProtocol>

/**
 设置控制台log输出的等级，debug默认输出info以下所有等级，其他默认输出error等级
 */
@property (nonatomic , class, assign) NSUInteger consoleLogLevel;

- (instancetype)initWithTag:(NSString *)tag;

@end
