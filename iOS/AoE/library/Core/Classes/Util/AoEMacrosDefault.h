//
//  AoEMacrosDefault.h
//  AoE
//
//  Created by dingchao on 2019/3/21.
//
#ifndef AEMacrosDefault_h
#define AEMacrosDefault_h

#import <Foundation/Foundation.h>
#if __has_include("AoElogger.h")
#import "AoElogger.h"
#endif

#define SafeBlockRun(block, ...) block ? block(__VA_ARGS__) : nil
#define APP_VERSION      [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"]

#ifdef DEBUG
#   define NSLog(fmt, ...) NSLog((@"[File %s] [Method %s] [Line %d] " fmt), __FILE__, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
#else
#if __has_include("AoElogger.h")
#   define NSLog(...) LogVerbose((@"[File %s] [Method %s] [Line %d] " fmt), __FILE__, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
#endif
#   define NSLog(...)
#endif
#define ALog(fmt, ...) NSLog((@"[File %s] [Method %s] [Line %d] " fmt), __FILE__, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)


#endif /* AEMacrosDefault_h */
