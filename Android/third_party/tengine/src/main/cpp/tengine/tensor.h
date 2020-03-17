//
// Created by Kris on 2020-03-06.
// @author fire9953@gmail.com
//

#ifndef AOE_ANDROID_TENSOR_H
#define AOE_ANDROID_TENSOR_H

#include "c_api_internal.h"
#include "tengine_c_api.h"

class TensorHandle {
public:

    TensorHandle();

    ~TensorHandle();

    inline tensor_t getTensor() {
        return mTensor;
    }

    void setTensor(tensor_t aTensor);

    inline TengineType getDataType() {
        return dataType;
    }

    void *getTensorBuffer();

    int setTensorBuffer(void *buffer, int bufferSize);

    int getTensorBufferSize();

    inline int getShapeW() {
        return mDims[3];
    };

    inline int getShapeH() {
        return mDims[2];
    };

    inline int getShapeC() {
        return mDims[1];
    };

    inline int getShapeDim() {
        return mDims[0];
    };

    int reshape(int dims[MAX_SHAPE_DIM_NUM]);

    int checkBufferAndMalloc();
    int checkBufferAndMalloc(int size);

private:
    tensor_t mTensor;
    TengineType dataType;
    int mDims[MAX_SHAPE_DIM_NUM];
    int needFreeBuffer;
};

#endif //AOE_ANDROID_TENSOR_H
