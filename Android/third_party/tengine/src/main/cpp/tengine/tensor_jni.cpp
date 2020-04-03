//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include "tensor_jni.h"
#include "c_api_internal.h"
#include "interpreter.h"
#include "tensor.h"
#include "jni_utils.h"
#include "tengine_operations.h"

using aoetengine::jni::ThrowException;

TensorHandle *castToTensor(JNIEnv *env, long handle) {
    if (handle == 0 && NULL != env) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<TensorHandle *>(handle);
}

JNIEXPORT jlong JNICALL
TENGINEJNI_METHOD2(create)(JNIEnv *env, jclass clazz, jlong interpreterHandle, jint tensorIndex,
                        jboolean isInput) {
    InterpreterHandler *interpreter = reinterpret_cast<InterpreterHandler *>(interpreterHandle);
    if (isInput) {
        return interpreter->inputs[tensorIndex];
    }

    return interpreter->outputs[tensorIndex];
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD2(delete)(JNIEnv *env, jclass clazz, jlong handle) {
    delete reinterpret_cast<TensorHandle *>(handle);
}

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD2(inputRgbaResize)(JNIEnv *env, jclass clazz, jbyteArray rgbaDate,
                                 jint srcWidth, jint srcHeight, jint dstWidth, jint dstHeight,
                                 jfloatArray channelMeanVals, jboolean toBgr, jfloat scale,
                                 jlong handler) {
    TensorHandle *tensor = castToTensor(env, handler);
    int ret = tensor->checkBufferAndMalloc(sizeof(float) * dstHeight * dstWidth * 3);
    if (ret != 0) {
        // buffer error
        return ret;
    }

    jbyte *imageDataByte = env->GetByteArrayElements(rgbaDate, NULL);
    jfloat *meanValsElements = NULL;
    if (NULL != channelMeanVals) {
        meanValsElements = env->GetFloatArrayElements(channelMeanVals, NULL);
    }

    int c = 3;
    const int inputC = 4;
    int h = srcHeight;
    int w = srcWidth;
    int dst_index;
    int src_index;

    unsigned char *imageData = (unsigned char *) imageDataByte;
    image imgT = make_image(w, h, c);
    for (int k = 0; k < c; ++k) {
        for (int j = 0; j < h; ++j) {
            for (int i = 0; i < w; ++i) {
                dst_index = i + w * j + w * h * k;
                src_index = k + inputC * i + inputC * w * j;
                imgT.data[dst_index] = (float) (imageData[src_index]);
            }
        }
    }

    image resImg = resize_image(imgT, dstWidth, dstHeight);
    free_image(imgT);
    if (toBgr) {
        resImg = rgb2bgr_premute(resImg);
    }

    float *img_data = resImg.data;
    int hw = dstHeight * dstWidth;

    float *input_data = static_cast<float *>(tensor->getTensorBuffer());
    if (NULL != meanValsElements) {
        for (int c = 0; c < 3; c++)
            for (int h = 0; h < dstHeight; h++)
                for (int w = 0; w < dstWidth; w++) {
                    // input_data为整个channel b  整个channel g 整个channel r
                    input_data[c * hw + h * dstWidth + w] =
                            (*img_data - meanValsElements[c]) * scale;
                    img_data++;
                }
    }
    free_image(resImg);

    env->ReleaseByteArrayElements(rgbaDate, imageDataByte, 0);
    if (NULL != meanValsElements) {
        env->ReleaseFloatArrayElements(channelMeanVals, meanValsElements, 0);
    }

    return 0;
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD2(writeDirectBuffer)(JNIEnv *env, jclass clazz, jlong handle, jobject src) {
    TensorHandle *tensor = castToTensor(env, handle);
    if (tensor == nullptr) return;

    char *src_data_raw = static_cast<char *>(env->GetDirectBufferAddress(src));
    if (!src_data_raw) {
        ThrowException(env, kIllegalArgumentException,
                       "Input ByteBuffer is not a direct buffer");
        return;
    }

    tensor->setTensorBuffer(src_data_raw, env->GetDirectBufferCapacity(src));
}

JNIEXPORT jobject JNICALL
TENGINEJNI_METHOD2(buffer)(JNIEnv *env, jclass clazz, jlong handler) {
    TensorHandle *tensor = castToTensor(env, handler);
    tensor->checkBufferAndMalloc();
    if (tensor->getTensorBuffer() == nullptr) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Tensor hasn't been allocated.");
        return nullptr;
    }

    return env->NewDirectByteBuffer(tensor->getTensorBuffer(),
                                    static_cast<jlong>(tensor->getTensorBufferSize()));
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD2(resizeInputShape)(JNIEnv *env, jclass clazz, jintArray dims, jlong handle) {
    TensorHandle *tensor = castToTensor(env, handle);
    if (tensor == nullptr) return;

    int *dimElements = env->GetIntArrayElements(dims, NULL);
    tensor->reshape(dimElements);

    if (NULL != dimElements) {
        env->ReleaseIntArrayElements(dims, dimElements, 0);
    }
}

JNIEXPORT jintArray JNICALL
TENGINEJNI_METHOD2(shape)(JNIEnv *env, jclass clazz, jlong handle) {
    TensorHandle *tensor = castToTensor(env, handle);
    if (tensor == nullptr) return nullptr;

    int num_dims = 4;
    int dimsArray[num_dims];
    dimsArray[0] = tensor->getShapeW();
    dimsArray[1] = tensor->getShapeH();
    dimsArray[2] = tensor->getShapeC();
    dimsArray[3] = tensor->getShapeDim();

    jintArray result = env->NewIntArray(num_dims);
    env->SetIntArrayRegion(result, 0, num_dims, dimsArray);
    return result;
}

JNIEXPORT jint JNICALL
TENGINEJNI_METHOD2(numBytes)(JNIEnv *env, jclass clazz, jlong handle) {
    TensorHandle *tensor = castToTensor(env, handle);
    if (tensor == nullptr) return 0;
    return static_cast<jint>(tensor->getTensorBufferSize());
}

size_t ElementByteSize(TengineType data_type) {
    // The code in this file makes the assumption that the
    // TensorFlow TF_DataTypes and the Java primitive types
    // have the same byte sizes. Validate that:
    switch (data_type) {
        case kTengineFloat32:
//            static_assert(sizeof(jfloat) == 4,
//                          "Interal error: Java float not compatible with "
//                          "kNcnnFloat");
            return 4;

        case kTengineFloat16:
            return 2;

        case kTengineInt8:
        case kTengineUInt8:
            return 1;

        case kTengineInt32:
            return 4;

        case kTengineInt16:
            return 2;

        default:
            return 0;
    }
}

size_t ReadOneDimensionalArray(JNIEnv *env, TengineType data_type,
                               const void *src, size_t src_size, jarray dst) {
    const int len = env->GetArrayLength(dst);
    const size_t size = len * ElementByteSize(data_type);
    if (size > src_size) {
        ThrowException(
                env, kIllegalStateException,
                "Internal error: cannot fill a Java array of %d bytes with a ncnn of "
                "%d bytes",
                size, src_size);
        return 0;
    }

    switch (data_type) {
        case kTengineFloat32: {
            jfloatArray float_array = static_cast<jfloatArray>(dst);
            env->SetFloatArrayRegion(float_array, 0, len,
                                     static_cast<const jfloat *>(src));
            return size;
        }
        case kTengineInt32: {
            jintArray int_array = static_cast<jintArray>(dst);
            env->SetIntArrayRegion(int_array, 0, len, static_cast<const jint *>(src));
            return size;
        }

        case kTengineInt16: {
            jshortArray short_array = static_cast<jshortArray>(dst);
            env->SetShortArrayRegion(short_array, 0, len,
                                     static_cast<const jshort *>(src));
            return size;
        }

        case kTengineUInt8:
        case kTengineInt8: {
            jbyteArray byte_array = static_cast<jbyteArray>(dst);
            env->SetByteArrayRegion(byte_array, 0, len,
                                    static_cast<const jbyte *>(src));
            return size;
        }
        default: {
            ThrowException(env, kIllegalStateException,
                           "DataType error: invalid DataType(%d)", data_type);
        }
    }
    return 0;
}

size_t ReadMultiDimensionalArray(JNIEnv *env, TengineType data_type, char *src,
                                 size_t src_size, int dims_left, jarray dst) {
    if (dims_left == 1) {
        return ReadOneDimensionalArray(env, data_type, src, src_size, dst);
    } else {
        jobjectArray ndarray = static_cast<jobjectArray>(dst);
        int len = env->GetArrayLength(ndarray);
        size_t size = 0;
        for (int i = 0; i < len; ++i) {
            jarray row = static_cast<jarray>(env->GetObjectArrayElement(ndarray, i));
            size += ReadMultiDimensionalArray(env, data_type, src + size,
                                              src_size - size, dims_left - 1, row);
            env->DeleteLocalRef(row);
            if (env->ExceptionCheck()) return size;
        }
        return size;
    }
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD2(readMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler,
                                           jobject value) {
    TensorHandle *tensor = castToTensor(env, handler);
    if (tensor->getShapeDim() == 0) {
        // dims 0
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Cannot copy empty/scalar Tensors.");
        return;
    }


    ReadMultiDimensionalArray(env, tensor->getDataType(), (char *) tensor->getTensorBuffer(),
                              tensor->getTensorBufferSize(),
                              tensor->getShapeDim(),
                              static_cast<jarray>(value));
}

size_t WriteOneDimensionalArray(JNIEnv *env, jobject object, TengineType type,
                                void *dst, size_t dst_size) {
    jarray array = static_cast<jarray>(object);
    const int num_elements = env->GetArrayLength(array);
    size_t to_copy = num_elements * ElementByteSize(type);
    if (to_copy > dst_size) {
        ThrowException(env, kIllegalStateException,
                       "Internal error: cannot write Java array of %d bytes to "
                       "Tensor of %d bytes",
                       to_copy, dst_size);
        return 0;
    }
    switch (type) {
        case kTengineFloat32: {
            jfloatArray float_array = static_cast<jfloatArray>(array);
            jfloat *float_dst = static_cast<jfloat *>(dst);
            env->GetFloatArrayRegion(float_array, 0, num_elements, float_dst);
            return to_copy;
        }

        case kTengineInt8:
        case kTengineUInt8: {
            jbyteArray byte_array = static_cast<jbyteArray>(array);
            jbyte *byte_dst = static_cast<jbyte *>(dst);
            env->GetByteArrayRegion(byte_array, 0, num_elements, byte_dst);
            return to_copy;
        }
        case kTengineInt32: {
            jintArray int_array = static_cast<jintArray>(array);
            jint *int_dst = static_cast<jint *>(dst);
            env->GetIntArrayRegion(int_array, 0, num_elements, int_dst);
            return to_copy;
        }
        case kTengineInt16: {
            jshortArray int_array = static_cast<jshortArray>(array);
            jshort *int_dst = static_cast<jshort *>(dst);
            env->GetShortArrayRegion(int_array, 0, num_elements, int_dst);
            return to_copy;
        }
        default: {
            ThrowException(env, kUnsupportedOperationException,
                           "DataType error: currently supports float "
                           "(32 bits), int (32 bits), byte (8 bits) "
                           "support for other types (DataType %d in this "
                           "case) will be added in the future",
                           type);
            return 0;
        }
    }
}

size_t WriteMultiDimensionalArray(JNIEnv *env, jobject src, TengineType type,
                                  int dims_left, char **dst, int dst_size) {
    if (dims_left <= 1) {
        return WriteOneDimensionalArray(env, src, type, *dst, dst_size);
    } else {
        jobjectArray ndarray = static_cast<jobjectArray>(src);
        int len = env->GetArrayLength(ndarray);
        size_t sz = 0;
        for (int i = 0; i < len; ++i) {
            jobject row = env->GetObjectArrayElement(ndarray, i);
            char *next_dst = *dst + sz;
            sz += WriteMultiDimensionalArray(env, row, type, dims_left - 1, &next_dst,
                                             dst_size - sz);
            env->DeleteLocalRef(row);
            if (env->ExceptionCheck()) return sz;
        }
        return sz;
    }
}

JNIEXPORT void JNICALL
TENGINEJNI_METHOD2(writeMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler, jobject src) {
    TensorHandle *tensor = castToTensor(env, handler);
    if (tensor == nullptr) return;
    if (tensor->getTensorBuffer() == nullptr) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Target Tensor hasn't been allocated.");
        return;
    }

    if (tensor->getShapeDim() == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Cannot copy empty/scalar Tensors.");
        return;
    }

    char *data = (char *) tensor->getTensorBuffer();
    WriteMultiDimensionalArray(env, src, tensor->getDataType(), tensor->getShapeDim(),
                               &data, tensor->getTensorBufferSize());
}
