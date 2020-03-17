//
// Created by Kris on 2020-03-06.
// @author fire9953@gmail.com
//

#include "tensor.h"
#include <stdlib.h>

TensorHandle::TensorHandle() {
}

TensorHandle::~TensorHandle() {
    if (needFreeBuffer) {
        void *buffer = getTensorBuffer();
        free(buffer);
    }

    release_graph_tensor(mTensor);
}

void TensorHandle::setTensor(tensor_t aTensor) {
    mTensor = aTensor;
    get_tensor_shape(mTensor, mDims, MAX_SHAPE_DIM_NUM);

    int type = get_tensor_data_type(mTensor);
    dataType = TengineType(type);
}

void *TensorHandle::getTensorBuffer() {
    return get_tensor_buffer(mTensor);
}

int TensorHandle::setTensorBuffer(void *buffer, int bufferSize) {
    return set_tensor_buffer(mTensor, buffer, bufferSize);
}

int TensorHandle::getTensorBufferSize() {
    return get_tensor_buffer_size(mTensor);
}

int TensorHandle::checkBufferAndMalloc() {
    if (NULL == getTensorBuffer()) {
        const int bufferSize = getTensorBufferSize();
        if (bufferSize <= 0) {
            return -1;
        }
        void *buffer = calloc(bufferSize, 1);
        if (NULL == buffer) {
            return -1;
        }

        needFreeBuffer = 1;
        return setTensorBuffer(buffer, bufferSize);
    }

    return 0;
}

int TensorHandle::checkBufferAndMalloc(int bufferSize) {
    if (NULL != getTensorBuffer()) {
        if (bufferSize != getTensorBufferSize()) {
            free(getTensorBuffer());
        } else {
            return 0;
        }
    }

    void *buffer = calloc(bufferSize, 1);
    if (NULL == buffer) {
        return -1;
    }

    needFreeBuffer = 1;
    return setTensorBuffer(buffer, bufferSize);
}

int TensorHandle::reshape(int dims[MAX_SHAPE_DIM_NUM]) {
    int ret = set_tensor_shape(mTensor, dims, MAX_SHAPE_DIM_NUM);
    if (ret != 0) {
        return ret;
    }

    ret = get_tensor_shape(mTensor, mDims, MAX_SHAPE_DIM_NUM);
    return ret;
}
