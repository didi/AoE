#include "squeeze.h"
#include "net.h"
#include "cJSON.h"
#include "AOELog.h"

#include <stdio.h>
#include <algorithm>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

static int print_topk(const std::vector<float> &cls_scores, int topk)
{
    // partial sort topk with index
    int size = cls_scores.size();
    std::vector<std::pair<float, int>> vec;
    vec.resize(size);
    for (int i = 0; i < size; i++)
    {
        vec[i] = std::make_pair(cls_scores[i], i);
    }

    std::partial_sort(vec.begin(), vec.begin() + topk, vec.end(),
                      std::greater<std::pair<float, int>>());

    // print topk and score
    for (int i = 0; i < topk; i++)
    {
        float score = vec[i].first;
        int index = vec[i].second;
        fprintf(stderr, "%d = %f\n", index, score);
    }

    return 0;
}

ncnn::Net squeezenet;

int parseConfig(const char *json_string, AOEModelOption *option)
{
    cJSON *cjson = cJSON_Parse(json_string);
    if (NULL == cjson)
    {
        return -1;
    }

    SqueezeModelOption *squeezeOption = (SqueezeModelOption *)option;
    strcpy(squeezeOption->runtime, cJSON_GetObjectItem(cjson, "runtime")->valuestring);
    strcpy(squeezeOption->tag, cJSON_GetObjectItem(cjson, "tag")->valuestring);
    strcpy(squeezeOption->version, cJSON_GetObjectItem(cjson, "version")->valuestring);
    strcpy(squeezeOption->modelDir, cJSON_GetObjectItem(cjson, "modelDir")->valuestring);
    strcpy(squeezeOption->modelFileName, cJSON_GetObjectItem(cjson, "modelFileName")->valuestring);
    strcpy(squeezeOption->modelParamFileName, cJSON_GetObjectItem(cjson, "modelParamFileName")->valuestring);

    cJSON_Delete(cjson);

    return 0;
}

int readConfigFileAndParse(const char *configFilePath, AOEModelOption *option)
{
    int step = 1024 * 10;

    FILE *inputFile = fopen(configFilePath, "r");
    if (NULL == inputFile)
    {
        fclose(inputFile);
        return -1;
    }

    char *data = NULL;
    int i = 0;
    while (1)
    {
        data = (char *)realloc(data, step * (i + 1));
        if (data == NULL)
        {
            fclose(inputFile);
            return -1;
        }

        int readLen = fread(data + (step * i), sizeof(uint8_t), step, inputFile);
        if (readLen == 0)
        {
            break;
        }

        i++;
    }

    parseConfig(data, option);
    free(data);
    fclose(inputFile);

    return 0;
}

int AOEReactor_fun_initModelOption(const char *configFilePath, AOEModelOption *option)
{
    int ret = readConfigFileAndParse(configFilePath, option);
    return ret;
}

int AOEReactor_fun_decryptAoeFileToFile(const char *srcFile, const char *dstFile)
{
    return 0;
}

int AOEReactor_fun_loadModel(AOEModelOption *option, AOECrypto *crypto)
{
    char paramPath[200];
    char modelPath[200];

    memset(paramPath, 0, sizeof(paramPath));
    memset(modelPath, 0, sizeof(modelPath));

    SqueezeModelOption *squeezeOption = (SqueezeModelOption *)option;
    strcat(paramPath, option->modelDir);
    strcat(paramPath, squeezeOption->modelParamFileName);

    strcat(modelPath, option->modelDir);
    strcat(modelPath, squeezeOption->modelFileName);

    if (NULL != crypto && NULL != crypto->decryptoFileCallback)
    {
        // TODO
    }

    int ret = squeezenet.load_param(paramPath);
    if (ret != 0)
    {
        return ret;
    }

    ret = squeezenet.load_model(modelPath);
    if (ret != 0)
    {
        return ret;
    }

    return 0;
}

int AOEReactor_fun_interpreterRun(const unsigned char *input, int w, int h)
{
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(input, ncnn::Mat::PIXEL_BGR, w, h, 227, 227);

    const float mean_vals[3] = {104.f, 117.f, 123.f};
    in.substract_mean_normalize(mean_vals, 0);

    ncnn::Extractor ex = squeezenet.create_extractor();
    int ret = ex.input("data", in);

    ncnn::Mat out;
    ret = ex.extract("prob", out);

    std::vector<float> cls_scores;
    cls_scores.resize(out.w);
    for (int j = 0; j < out.w; j++)
    {
        cls_scores[j] = out[j];
    }

    print_topk(cls_scores, 3);
    return ret;
}

int main(int argc, char **argv)
{
    if (argc != 3)
    {
        fprintf(stderr, "Usage: %s [modelconfigpath] [imagepath]\n", argv[0]);
        return -1;
    }

    AOEModelLoader modelLoader;
    modelLoader.initModelOptionCallback = AOEReactor_fun_initModelOption;
    modelLoader.loadModelCallback = AOEReactor_fun_loadModel;
    AOEReactor_registModelLoaderHandler(&modelLoader);

    AOECrypto crypto;
    crypto.decryptoFileCallback = AOEReactor_fun_decryptAoeFileToFile;
    AOEReactor_registCryptoHandler(&crypto);

    AOEInterpreter aoeinterpreter;
    aoeinterpreter.interceptorCallback = AOEReactor_fun_interpreterRun;
    AOEReactor_registInterpreterHandler(&aoeinterpreter);

    const char *configPath = argv[1];
    SqueezeModelOption option;
    int ret = AOEReactor_initModelOption(configPath, &option);
    SLOG_D("init option ret: %d \n", ret);
    if (ret != 0)
    {
        fprintf(stderr, "initModelOption %d failed\n", ret);
        return -1;
    }

    ret = AOEReactor_loadModel(&option);
    SLOG_D("load model ret: %d \n", ret);

    if (ret != 0)
    {
        fprintf(stderr, "loadModel %d failed\n", ret);
        return -1;
    }

    const char *imagepath = argv[2];
    cv::Mat bgr = cv::imread(imagepath, 1);
    if (bgr.empty())
    {
        fprintf(stderr, "cv::imread %s failed\n", imagepath);
        return -1;
    }

    ret = AOEReactor_run(bgr.data, bgr.cols, bgr.rows);
    SLOG_D("run ret: %d \n", ret);

    return 0;
}