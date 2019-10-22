//
//  aoesign.h
//  AoE
//
//  Created by dingchao on 2019/10/21.
//

#ifndef aoesign_h
#define aoesign_h

#include <stdio.h>

struct aoeSignParams
{
    char *strToSign;
    char *appKey;
    int strToSignLenght;
    int appKeyLenght;
    int method;
};

/// aoe生成通用签名
/// @param paramStr 要加密的参数拼接的string
/// @param paramStrLenght 参数拼接string长度
/// @param appKey appkey
/// @param appKeyLenght appkey长度
/// @param method 签名方法 （暂时没有使用）
/// @param signedStr 回调
int aoe_generalSign(const char *paramStr, const unsigned long paramStrLenght,const char *appKey , const unsigned long appKeyLenght, int method, char **signedStr);
#endif /* aoesign_h */
