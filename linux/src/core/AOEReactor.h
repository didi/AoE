#ifndef CORE_AOEREACTOR_H_
#define CORE_AOEREACTOR_H_

typedef struct
{
    char version[10];
    char tag[30];
    char runtime[20];
    char modelDir[128];
} AOEModelOption;

typedef int AOEReactorCallback_decryptAoeFileToFile(const char *srcFile, const char *dstFile);
typedef int AOEReactorCallback_decryptAoeFileToMem(const char *srcFile, char **dstMem);

typedef struct
{
    AOEReactorCallback_decryptAoeFileToFile *decryptoFileCallback;
    AOEReactorCallback_decryptAoeFileToMem *decryptoMemCallback;
} AOECrypto;

typedef int AOEReactorCallback_initModelOption(const char *configFilePath, AOEModelOption *option);
typedef int AOEReactorCallback_loadModel(AOEModelOption *option, AOECrypto *crypto);
typedef int AOEReactorCallback_interpreterRun(const unsigned char *input, int w, int h);

typedef struct
{
    AOEReactorCallback_initModelOption *initModelOptionCallback;
    AOEReactorCallback_loadModel *loadModelCallback;
} AOEModelLoader;

typedef struct
{
    AOEReactorCallback_interpreterRun *interceptorCallback;
} AOEInterpreter;

#ifdef __cplusplus
extern "C"
{
#endif

    int AOEReactor_registModelLoaderHandler(AOEModelLoader *loader);
    int AOEReactor_registCryptoHandler(AOECrypto *crypto);
    int AOEReactor_registInterpreterHandler(AOEInterpreter *interpreter);

    int AOEReactor_initModelOption(const char *configFilePath, AOEModelOption *option);
    int AOEReactor_loadModel(AOEModelOption *option);
    int AOEReactor_run(const unsigned char *input, int w, int h);
    int AOEReactor_release(void);

#ifdef __cplusplus
}
#endif

#endif