//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include <android/log.h>
#include <unistd.h>

#include "nativeinterpreterwrapper_jni.h"
#include "c_api_internal.h"
#include "interpreter.h"
#include "tensor.h"
#include "jni_utils.h"
#include "tengine_operations.h"

using aoetengine::jni::ThrowException;

static int tengingedInited = 0;

InterpreterHandler* castToInterpreter(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<InterpreterHandler *>(handle);
}

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(createInterpreter)(JNIEnv *env, jclass clazz) {
    if (tengingedInited == 0) {
        init_tengine();
        tengingedInited = 1;
    }

    InterpreterHandler *handler = new InterpreterHandler();
    return reinterpret_cast<jlong> (handler);
}

JNIEXPORT jboolean JNICALL
NCNNJNI_METHOD(loadTengineModelFromPath)(JNIEnv *env, jclass clazz, jstring aModelPath,
                                  jlong interpreterHandle) {
    const char *modelPath = env->GetStringUTFChars(aModelPath, 0);

    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    const int model_ret = interpreter->loadTengineModel(modelPath);
    return (model_ret == 0);
}

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD(allocateTensors)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    int size = interpreter->getInputCount();
    for (int i = 0; i < size; i++) {
        TensorHandle *handle = new TensorHandle();
        handle->setTensor(get_graph_input_tensor(interpreter->getGraph(), 0, i));
        interpreter->inputs.push_back(reinterpret_cast<long>(handle));
    }

    size = interpreter->getOutputCount();
    for (int i = 0; i < size; i++) {
        TensorHandle *handle = new TensorHandle();
        interpreter->outputs.push_back(reinterpret_cast<long>(handle));
    }

    return 0;
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getInputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    return interpreter->getInputCount();
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(getOutputCount)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    return interpreter->getOutputCount();
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD(run)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    graph_t graph = interpreter->getGraph();

    int ret_prerun = prerun_graph(graph);
    if (ret_prerun < 0) {
        return -1;
    }

    int ret = run_graph(graph, 1);
    int size = interpreter->getOutputCount();
    for (int i = 0; i < size; i++) {
        TensorHandle *tensorHandle = interpreter->getTensor(env, interpreter->outputs[i]);
        tensor_t output_tensor = get_graph_output_tensor(graph, i, 0);
        tensorHandle->setTensor(output_tensor);
    }

    return ret;
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD(delete)(JNIEnv *env, jclass clazz, jlong interpreterHandle) {
    InterpreterHandler *interpreter = castToInterpreter(env, interpreterHandle);
    delete (interpreter);
    interpreter = NULL;
}
