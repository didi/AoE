//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include <android/log.h>
#include <unistd.h>

#include "nativeinterpreterwrapper_jni.h"
#include "net.h"
#include "c_api_internal.h"
#include "interpreter.h"
#include "jni_utils.h"

using aoencnn::jni::ThrowException;

InterpreterHandler *getInterpreter(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<InterpreterHandler *>(handle);
}

TensorHandle *getTensor(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<TensorHandle *>(handle);
}


JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(createInterpreter)(JNIEnv *env, jclass clazz, jboolean lightMode, jint numThreads) {
    InterpreterHandler *handler = new InterpreterHandler();
    handler->setThreadNum(numThreads);
    handler->setLightMode(lightMode);
    return reinterpret_cast<jlong> (handler);
}

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadModel)(JNIEnv *env, jclass clazz, jstring aFilePath, jlong interpreterHandle) {
    const char *filePath = env->GetStringUTFChars(aFilePath, 0);
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->mNet.load_model(filePath);
    return (model_ret > 0);
}

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadParam)(JNIEnv *env, jclass clazz, jstring aFilePath, jlong interpreterHandle) {
    const char *filePath = env->GetStringUTFChars(aFilePath, 0);
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->mNet.load_param(filePath);
    return (model_ret > 0);
}

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadModelFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
                                    jstring aFolderName, jstring aFileName,
                                    jlong interpreterHandle) {
    const char *folderName = env->GetStringUTFChars(aFolderName, 0);
    const char *fileName = env->GetStringUTFChars(aFileName, 0);


    AAssetManager *nativeAsset = AAssetManager_fromJava(env, assetManager);
    std::string targetName = std::string(folderName) + "/" + std::string(fileName);

    AAsset *asset = AAssetManager_open(nativeAsset, targetName.data(), AASSET_MODE_BUFFER);
    if (NULL == asset) {
        return false;
    }

    auto *mem = (const unsigned char *) AAsset_getBuffer(asset);
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->mNet.load_model(mem);
    return (model_ret > 0);
}

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadParamFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
                                    jstring aFolderName, jstring aFileName,
                                    jlong interpreterHandle) {
    const char *folderName = env->GetStringUTFChars(aFolderName, 0);
    const char *fileName = env->GetStringUTFChars(aFileName, 0);

    AAssetManager *nativeAsset = AAssetManager_fromJava(env, assetManager);
    std::string targetName = std::string(folderName) + "/" + std::string(fileName);

    AAsset *asset = AAssetManager_open(nativeAsset, targetName.data(), AASSET_MODE_BUFFER);
    if (NULL == asset) {
        return false;
    }

    auto *mem = (const unsigned char *) AAsset_getBuffer(asset);
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->mNet.load_param(mem);
    return (model_ret > 0);
}

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(allocateTensors)(JNIEnv *env, jclass clazz, jlong interpreterHandle, jint inputCount,
                                jint outputCount) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    int size = inputCount;
    for (int i = 0; i < size; i++) {
        TensorHandle *handle = new TensorHandle();
        interpreter->inputs.push_back(reinterpret_cast<long>(handle));
    }

    size = outputCount;
    for (int i = 0; i < size; i++) {
        TensorHandle *handle = new TensorHandle();
        interpreter->outputs.push_back(reinterpret_cast<long>(handle));
    }

    return 0;
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD(setBlobIndex)(JNIEnv *env, jclass clazz, jint inputBlobIndex, jint outputBlobIndex,
                             jlong interpreterHandle) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    interpreter->setBlobIndex(inputBlobIndex, outputBlobIndex);
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getInputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    return interpreter->inputs.size();
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getOutputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);
    return interpreter->outputs.size();
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD(inputRgba)(JNIEnv *env, jclass clazz, jbyteArray rgbaDate,
                          jint srcWidth, jint srcHeight, jint dstWidth, jint dstHeight,
                          jfloatArray meanVals, jfloatArray normVals, jlong interpreterHandle,
                          jlong tensorHandle) {
    jbyte *imageDataByte = env->GetByteArrayElements(rgbaDate, NULL);

    jfloat *meanValsElements = NULL;
    if (NULL != meanVals) {
        meanValsElements = env->GetFloatArrayElements(meanVals, NULL);
    }

    jfloat *normValsElements = NULL;
    if (NULL != normVals) {
        normValsElements = env->GetFloatArrayElements(normVals, NULL);
    }

    TensorHandle *tensor = getTensor(env, tensorHandle);
    *(tensor->getMat()) = ncnn::Mat::from_pixels_resize((unsigned char *) imageDataByte,
                                                        ncnn::Mat::PIXEL_RGBA2BGR,
                                                        srcWidth, srcHeight,
                                                        dstWidth, dstHeight);

    tensor->getMat()->substract_mean_normalize(meanValsElements, normValsElements);
    env->ReleaseByteArrayElements(rgbaDate, imageDataByte, 0);
    if (NULL != meanValsElements) {
        env->ReleaseFloatArrayElements(meanVals, meanValsElements, 0);
    }

    if (NULL != normValsElements) {
        env->ReleaseFloatArrayElements(normVals, normValsElements, 0);
    }
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(run)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);

    ncnn::Extractor ex = interpreter->mNet.create_extractor();
    ex.set_light_mode(interpreter->isLightMode());
    ex.set_num_threads(interpreter->getThreadNum());

    uint64_t size = interpreter->inputs.size();
    for (int i = 0; i < size; i++) {
        ex.input(interpreter->getInputBlobIndex(),
                 *(getTensor(env, interpreter->inputs[i])->getMat()));
    }

    size = interpreter->outputs.size();
    for (int i = 0; i < size; i++) {
        ex.extract(interpreter->getOutputBlobIndex(),
                   *(getTensor(env, interpreter->outputs[i])->getMat()));
    }

    return 0;
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD(delete)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = getInterpreter(env, interpreterHandle);

    uint64_t size = interpreter->inputs.size();
    for (int i = 0; i < size; i++) {
        delete (getTensor(env, interpreter->inputs[i]));
    }

    size = interpreter->outputs.size();
    for (int i = 0; i < size; i++) {
        delete (getTensor(env, interpreter->outputs[i]));
    }

    delete (interpreter);
}
