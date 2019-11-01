//
//  aoesign.h
//  AoE
//
//  Created by dingchao on 2019/10/21.
//

#ifndef aoesign_h
#define aoesign_h

#include <stdio.h>
#include "aoedict.h"

typedef struct dictEntryFake {
    aoe_dictEntry *real; /* simple singly linked list */
    int index;
} dictEntryFake;

typedef struct dictFake {
    dictEntryFake **table;
    unsigned long size;
} dictFake;

void releaseDictFake(dictFake *fakes);

/// 生成签名字典数据
/// @param len dict 元素个数
dictFake *createDictFake(int len);

/// 给字典添加Entry元素
/// @param fakes 字典对象
/// @param key Entry 的key
/// @param val Entry 的value
/// @param indx  当前字典添加次数 后期会删除
void addDictEntryFake(dictFake *fakes, const char *key, const char *val , int indx);

/// 使用dictFake 加密
/// @param fakes fakes 结构体指针
/// @param appKey 每个app的key
/// @param appKeyLenght appkey的长度
/// @param method 默认为0
/// @param signedStr 加密后的字符串
int aoe_generalSignDictFake(const dictFake *fakes,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);

/// aoe生成通用签名
/// @param paramStr 要加密的参数拼接的string
/// @param paramStrLenght 参数拼接string长度
/// @param appKey appkey
/// @param appKeyLenght appkey长度
/// @param method 签名方法 （暂时没有使用）
/// @param signedStr 回调
int aoe_generalSign(const char *paramStr, const unsigned long paramStrLenght,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);
#endif /* aoesign_h */
