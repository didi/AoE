//
//  AoElogger.m
//  AoE
//
//  Created by dingchao on 2019/7/3.
//

#import "AoElogger.h"

#ifdef DEBUG
static NSUInteger __consoleLogLevel = AoElogLevelTypeForInfo;
#else
static NSUInteger __consoleLogLevel = AoElogLevelTypeForError;
#endif


@interface AoElogger ()

@property (nonatomic , strong) NSString *tag;

- (void)consolelogMsg:(NSString *)content level:(NSInteger)level;

@end

void AoElogMessageFunc(NSInteger level, const char *file,int lineNumber, const char *functionName, NSString *format, ...){
    va_list args;
    if (format) {
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:format arguments:args];
        NSString *fileMessage = [NSString stringWithFormat:@"file: [%s], line : [%@]",file,@(lineNumber)];
        message = [fileMessage stringByAppendingFormat:@" %@",message];
        [[AoElogger new] consolelogMsg:message level:level];
        va_end(args);
    }
}

@implementation AoElogger

- (instancetype)initWithTag:(NSString *)tag {
    self = [super init];
    if (self) {
        [self setLogTag:tag];
    }
    return self;
}

- (void)consolelogMsg:(NSString *)content level:(NSInteger)level {
    if (level <= __consoleLogLevel) {
        NSLog((@"[level %@] [Tag %@] %@"),@(level),self.tag?: [NSString stringWithUTF8String:__FILE__], content);
    }
}

- (void)debugLog:(NSString *)content {
    [self consolelogMsg:content level:AoElogLevelTypeForDebug];
}

- (void)errorLog:(NSString *)content {
    [self consolelogMsg:content level:AoElogLevelTypeForError];
}

- (void)warningLog:(NSString *)content {
    [self consolelogMsg:content level:AoElogLevelTypeForWarning];
}

- (void)infoLog:(NSString *)content {
    [self consolelogMsg:content level:AoElogLevelTypeForInfo];
}

- (void)setLogTag:(NSString *)tag {
    self.tag = tag;
}

#pragma mark - pravte

+ (NSUInteger)consoleLogLevel {
    return __consoleLogLevel;
}

+ (void)setConsoleLogLevel:(NSUInteger)consoleLogLevel {
    __consoleLogLevel = consoleLogLevel;
}

@end
