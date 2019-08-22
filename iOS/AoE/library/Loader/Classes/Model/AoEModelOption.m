//
//  AoEModelOption.m
//  AoE
//
//  Created by dingchao on 2019/3/20.
//

#import "AoEModelOption.h"
#import "AoEValidJudge.h"
#import <objc/runtime.h>

@implementation AoEModelOption



+ (instancetype)modelWithPath:(NSString *)path {
    return [[self alloc] initWithPath:path];
}

- (instancetype)initWithPath:(NSString *)path {
    NSFileManager *manager = [NSFileManager defaultManager];
    NSString *fileComponent = @"model.config";
    BOOL isDir = NO;
    BOOL isExist = [manager fileExistsAtPath:path isDirectory:&isDir];
    BOOL isReadableFile = NO;
    NSString *filePath = path;
    
    if ([path.pathExtension isEqualToString:fileComponent.pathExtension] &&
        [path.lastPathComponent isEqualToString:fileComponent]) {
        isReadableFile = YES;
    }else if (isDir && isExist) {
        filePath = [path stringByAppendingPathComponent:fileComponent];
        BOOL isExist = [manager fileExistsAtPath:path isDirectory:&isDir];
        if (isExist &&
            !isDir) {
           isReadableFile = YES;
        }
    }
    
    if (isReadableFile) {
        NSData *fileData = [[NSData alloc] initWithContentsOfFile:filePath];
        if (fileData) {
            NSString *content = [[NSString alloc] initWithData:fileData encoding:NSUTF8StringEncoding];
            self = (AoEModelOption *)[AoEModelOption ObjectFromJSONSerializationedString:content];
            self.modelPath = [filePath stringByDeletingLastPathComponent];
        }else {
            self = nil;
        }
    }else {
        self = nil;
    }
    return self;
}

- (AoEModelOption *)initWithDictionary:(NSDictionary *)opt {
    if (self = [super init]) {
        self.version = opt[@"version"];
        self.tag = opt[@"tag"];
        self.modelDir = opt[@"modelDir"];
        self.modelPath = opt[@"modelPath"];
        self.modelName = opt[@"modelName"];
        self.updateUrl = opt[@"updateUrl"];
        self.sign = opt[@"sign"];
        self.encrypted = ((NSNumber *)opt[@"encrypted"]).boolValue;
        self.encryptType = ((NSNumber *)opt[@"encryptType"]).integerValue;
    }
    return self;
}

-(AoEModelOption *)copyWithZone:(NSZone *)zone {
    AoEModelOption *option = [[AoEModelOption allocWithZone:zone] init];
    option.version = self.version;
    option.tag = self.tag;
    option.modelDir = self.modelDir;
    option.modelName = self.modelName;
    option.updateUrl = self.updateUrl;
    option.modelPath = self.modelPath;
    
    return option;
}

- (NSString *)description {
    NSString *des = [NSString stringWithFormat:@"version:[%@],\
                     tag:[%@],\
                     modelDir:[%@],\
                     modelName:[%@],\
                     modelPath:[%@],\
                     updateUrl:[%@],\
                     sign:[%@]",
                     self.version,
                     self.tag,
                     self.modelDir,
                     self.modelName,
                     self.modelPath,
                     self.updateUrl,
                     self.sign];
    return des;
}

#pragma getter and setter

- (void)setModelOptionLoaderClassName:(NSString *)className {
    objc_setAssociatedObject(self, @selector(modelOptionLoaderClassName), className, OBJC_ASSOCIATION_COPY);
}

- (NSString *)modelOptionLoaderClassName {
    return objc_getAssociatedObject(self, _cmd);
}

- (void)setInterpreterClassName:(NSString *)interpreterClassName {
    objc_setAssociatedObject(self, @selector(interpreterClassName), interpreterClassName, OBJC_ASSOCIATION_COPY);
}

- (NSString *)interpreterClassName {
    return objc_getAssociatedObject(self, _cmd);
}

- (BOOL)isValidOption {
    return ([AoEValidJudge isValidString:self.tag] &&
            [AoEValidJudge isValidString:self.modelDir] &&
            [AoEValidJudge isValidString:self.version] &&
            [AoEValidJudge isValidString:self.modelPath]);
}

- (NSString *)modelDirPath {
    return self.modelPath;
}

@end

@implementation AoEModelOption (AoE_Serializationed)

- (NSDictionary *)dictionarySerializationed {
    return @{@"version":(self.version?:@""),
             @"tag":(self.tag?:@""),
             @"modeldDir":(self.modelDir?:@""),
             @"modelPath":(self.modelPath?:@""),
             @"modelName":(self.modelName?:@""),
             @"sign":(self.sign?:@"")
             };
}

- (id)objectSerializationed {
    return [self dictionarySerializationed];
}

- (NSString *)JSONSerializationedString {
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:[self objectSerializationed]
                                                   options:NSJSONWritingPrettyPrinted
                                                     error:&error];
    if (!data ||
        error) {
        NSLog(@" error : %@", error);
    }
    return [[NSString alloc] initWithData:data
                                 encoding:NSUTF8StringEncoding];
}

+ (id<AoEModelOptionProtocol>)ObjectFromJSONSerializationedString:(NSString *)jsonString {
    if (![AoEValidJudge isValidString:jsonString]) {
        return nil;
    }
    NSError *error = nil;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    id jsonObject = [NSJSONSerialization JSONObjectWithData:data
                                                    options:NSJSONReadingMutableContainers
                                                      error:&error];
    if (!jsonObject ||
        ![jsonObject isKindOfClass:[NSDictionary class]] ||
        error) {
        NSLog(@" error : %@", error);
        return nil;
    }
    return [[self alloc] initWithDictionary:jsonObject];
}

+ (id<AoEModelOptionProtocol>)ObjectFromJSONSerializationedFilePath:(NSString *)filePath {
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        return nil;
    }
    
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    if (!data) {
        return nil;
    }
    NSError *error = nil;
    id jsonObject = [NSJSONSerialization JSONObjectWithData:data
                                                    options:NSJSONReadingMutableContainers
                                                      error:&error];
    if (!jsonObject ||
        ![jsonObject isKindOfClass:[NSDictionary class]] ||
        error) {
        NSLog(@" error : %@", error);
        return nil;
    }
    return [[self alloc] initWithDictionary:jsonObject];
}

@end
