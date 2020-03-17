//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include "interpreter.h"
#include "jni_utils.h"
#include "tensor.h"

using aoetengine::jni::ThrowException;

InterpreterHandler::InterpreterHandler() {
}

TensorHandle *InterpreterHandler::getTensor(JNIEnv *env, long handle) {
    if (handle == 0 && NULL != env) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<TensorHandle *>(handle);
}

InterpreterHandler::~InterpreterHandler() {
    uint64_t size = inputs.size();
    for (int i = 0; i < size; i++) {
        delete (getTensor(NULL, inputs[i]));
        inputs[i] = 0;
    }

    size = outputs.size();
    for (int i = 0; i < size; i++) {
        delete (getTensor(NULL, outputs[i]));
        outputs[i] = 0;
    }

    if (NULL != graph) {
        postrun_graph(graph);
        destroy_graph(graph);
    }
}

int InterpreterHandler::loadTengineModel(const char *tengineModelPath) {
    graph = create_graph(nullptr, "tengine", tengineModelPath);
    if (NULL == graph) {
        return -1;
    }

    LOGI("yangke run-time library version: %s ", get_tengine_version());
    mInputCount = get_graph_input_node_number(graph);
    mOutputCount = get_graph_output_node_number(graph);

    return 0;
}
