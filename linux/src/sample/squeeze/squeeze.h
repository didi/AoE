#ifndef _SQUEEZE_H_
#define _SQUEEZE_H_

#include "AOEReactor.h"

struct SqueezeModelOption : public AOEModelOption
{
    char modelFileName[50];
    char modelParamFileName[50];
};

#endif