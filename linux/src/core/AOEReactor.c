#include "AOEReactor.h"

#include <stddef.h>

AOEModelLoader *mLoader = NULL;
AOECrypto *mCrypto = NULL;
AOEInterpreter *mInterpreter = NULL;

int AOEReactor_registModelLoaderHandler(AOEModelLoader *loader)
{
    mLoader = loader;
    return 0;
}

int AOEReactor_registCryptoHandler(AOECrypto *crypto)
{
    mCrypto = crypto;
    return 0;
}

int AOEReactor_registInterpreterHandler(AOEInterpreter *interpreter)
{
    mInterpreter = interpreter;
    return 0;
}

int AOEReactor_initModelOption(const char *configFilePath, AOEModelOption *option)
{
    if (NULL == configFilePath || NULL == option)
    {
        return -1;
    }

    if (NULL != mLoader && NULL != mLoader->initModelOptionCallback)
    {
        return mLoader->initModelOptionCallback(configFilePath, option);
    }

    return -1;
}

int AOEReactor_loadModel(AOEModelOption *option)
{
    if (NULL != mLoader && NULL != mLoader->loadModelCallback)
    {
        return mLoader->loadModelCallback(option, mCrypto);
    }

    return -1;
}

int AOEReactor_run(const unsigned char *input, int w, int h)
{
    if (NULL != mInterpreter && NULL != mInterpreter->interceptorCallback)
    {
        return mInterpreter->interceptorCallback(input, w, h);
    }

    return -1;
}

int AOEReactor_release(void)
{
    mLoader = NULL;
    mInterpreter = NULL;
    mCrypto = NULL;
    return 0;
}