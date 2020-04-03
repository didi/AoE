//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include <android/log.h>
#include <unistd.h>
#include <string>

#include "nativeinterpreterwrapper_jni.h"
#include "c_api_internal.h"
#include "interpreter.h"
#include "tensor.h"
#include "jni_utils.h"
#include "tengine_operations.h"

using aoetengine::jni::ThrowException;

static int tengingedInited = 0;

InterpreterHandler *castToInterpreter(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<InterpreterHandler *>(handle);
}

JNIEXPORT jlong JNICALL
TENGINEJNI_METHOD(createInterpreter)(JNIEnv *env, jclass clazz) {
    if (tengingedInited == 0) {
        init_tengine();
        tengingedInited = 1;
    }

    InterpreterHandler *handler = new InterpreterHandler();
    return reinterpret_cast<jlong> (handler);
}

JNIEXPORT jstring JNICALL
TENGINEJNI_METHOD(getTengineVersion)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    if (tengingedInited == 0) {
        ThrowException(env, kIllegalStateException,
                       "Internal error: Tengine not init, please init it first.");
    }

    const char *version = get_tengine_version();
    return env->NewStringUTF(version);
}

JNIEXPORT jboolean JNICALL
TENGINEJNI_METHOD(loadTengineModelFromPath)(JNIEnv *env, jclass clazz, jstring aModelPath,
                                         jlong interpreterHandle) {
    const char *modelPath = env->GetStringUTFChars(aModelPath, 0);

    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->loadTengineModel(modelPath);
    return (model_ret == 0);
}

JNIEXPORT jboolean JNICALL
TENGINEJNI_METHOD(loadModelFromAssets)(JNIEnv *env, jclass clazz, jobject assetManager,
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

    const auto *mem = (const unsigned char *) AAsset_getBuffer(asset);
    const int size = AAsset_getLength(asset);
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->loadTengineModelMemory((const char *) mem, size);
    return (model_ret == 0);
}

JNIEXPORT jlong JNICALL
TENGINEJNI_METHOD(allocateTensors)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    int nodes = get_graph_input_node_number(interpreter->getGraph());
    for (int i = 0; i < nodes; i++) {
        int tensorCount = get_node_output_number(get_graph_input_node(interpreter->getGraph(), i));
        for (int j = 0; j < tensorCount; j++) {
            TensorHandle *handle = new TensorHandle();
            handle->setTensor(get_graph_input_tensor(interpreter->getGraph(), i, j));
            interpreter->inputs.push_back(reinterpret_cast<long>(handle));
        }
    }

    int size = interpreter->getOutputCount();
    for (int i = 0; i < size; i++) {
        TensorHandle *handle = new TensorHandle();
        interpreter->outputs.push_back(reinterpret_cast<long>(handle));
    }

    return 0;
}

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(getInputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    return interpreter->getInputCount();
}

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(getOutputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    return interpreter->getOutputCount();
}

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD(run)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    graph_t graph = interpreter->getGraph();

    int ret = run_graph(graph, 1);

    int nodes = get_graph_output_node_number(interpreter->getGraph());
    for (int i = 0; i < nodes; i++) {
        int tensorCount = get_node_output_number(get_graph_output_node(interpreter->getGraph(), i));
        for (int j = 0; j < tensorCount; j++) {
            TensorHandle *tensorHandle = interpreter->getTensor(env, interpreter->outputs[i]);
            tensor_t output_tensor = get_graph_output_tensor(graph, i, j);
            tensorHandle->setTensor(output_tensor);
        }
    }

    return ret;
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD(delete)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    delete (interpreter);
    interpreter = NULL;
}
