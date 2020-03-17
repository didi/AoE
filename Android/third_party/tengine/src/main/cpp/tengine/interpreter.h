//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_INTERPRETER_H
#define AOE_ANDROID_INTERPRETER_H

#include "c_api_internal.h"
#include "tengine_c_api.h"
#include "jni.h"
#include "tensor.h"
#include <vector>

class InterpreterHandler {
public:
    InterpreterHandler();

    ~InterpreterHandler();

    TensorHandle *getTensor(JNIEnv *env, long handle);

    int loadTengineModel(const char *tengineModelPath);

    inline int getInputCount() {
        return mInputCount;
    }

    inline int getOutputCount() {
        return mOutputCount;
    }

    inline graph_t getGraph() const {
        return graph;
    }

public:
    std::vector<long> inputs;
    std::vector<long> outputs;

private:
    graph_t graph;
    int mInputCount;
    int mOutputCount;
};

#endif //AOE_ANDROID_INTERPRETER_H
