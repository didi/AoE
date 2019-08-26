//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_INTERPRETER_H
#define AOE_ANDROID_INTERPRETER_H

#include "net.h"
#include "c_api_internal.h"

class InterpreterHandler {
public:
    InterpreterHandler();

    ~InterpreterHandler();

    inline int getThreadNum() const {
        return mThreadNum;
    }

    inline void setThreadNum(int threadNum) {
        mThreadNum = threadNum;
    }

    inline bool isLightMode() const {
        return mLightMode;
    }

    inline void setLightMode(bool lightMode) {
        mLightMode = lightMode;
    }

    inline void setBlobIndex(int inputBlobIndex, int outputBlobIndex) {
        mInputBlobIndex = inputBlobIndex;
        mOutputBlobIndex = outputBlobIndex;
    }

    inline int getInputBlobIndex() const {
        return mInputBlobIndex;
    }

    inline int getOutputBlobIndex() const {
        return mOutputBlobIndex;
    }

public:
    ncnn::Net mNet;
    std::vector<long> inputs;
    std::vector<long> outputs;

private:
    int mThreadNum;
    bool mLightMode;
    int mInputBlobIndex;
    int mOutputBlobIndex;
};

class TensorHandle {
public:

    TensorHandle();

    ~TensorHandle();

    inline ncnn::Mat *getMat() {
        return &mat;
    }

    inline NcnnType getDataType() {
        return dataType;
    }

private:
    ncnn::Mat mat;
    NcnnType dataType;
};

#endif //AOE_ANDROID_INTERPRETER_H
