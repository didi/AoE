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
dictFake *createDictFake(int len);
void addDictEntryFake(dictFake *fakes, const char *key, const char *val , int indx);
int aoe_generalSignDictFake(const dictFake *fakes,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);
/// 生成签名dict
aoe_dict * generalSignDict(void);

/// dict添加元素
/// @param ht dict对象
/// @param key key
/// @param keyLen 长度
/// @param val value
int aoe_generalDictAdd (aoe_dict *ht,const char *key, int keyLen, const char *val);

/// aoe生成通用签名
/// @param paramDict dict 实例
/// @param appKey appkey
/// @param appKeyLenght appkey长度
/// @param method 签名方法 （暂时没有使用）
/// @param signedStr 回调
int aoe_generalSignDict(const aoe_dict *paramDict,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);

/// aoe生成通用签名
/// @param paramStr 要加密的参数拼接的string
/// @param paramStrLenght 参数拼接string长度
/// @param appKey appkey
/// @param appKeyLenght appkey长度
/// @param method 签名方法 （暂时没有使用）
/// @param signedStr 回调
int aoe_generalSign(const char *paramStr, const unsigned long paramStrLenght,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);
#endif /* aoesign_h */
