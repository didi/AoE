//
// Created by kris on 2019/8/20.
// @author fire9953@gmail.com
//

#include "net.h"
#include "tensor_jni.h"
#include "c_api_internal.h"
#include "interpreter.h"
#include "jni_utils.h"

using aoencnn::jni::ThrowException;

TensorHandle *GetTensorFromHandle(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Invalid handle to TensorHandle.");
        return nullptr;
    }
    return reinterpret_cast<TensorHandle *>(handle);
}

JNIEXPORT jlong JNICALL
NCNNJNI_METHOD2(create)(JNIEnv *env, jclass clazz, jlong interpreterHandle, jint tensorIndex,
                       jboolean isInput) {
    InterpreterHandler *interpreter = reinterpret_cast<InterpreterHandler *>(interpreterHandle);
    if (isInput) {
        return interpreter->inputs[tensorIndex];
    }

    return interpreter->outputs[tensorIndex];
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(delete)(JNIEnv *env, jclass clazz, jlong handle) {
    delete reinterpret_cast<TensorHandle *>(handle);
}

JNIEXPORT void JNICALL
NCNNJNI_METHOD2(writeDirectBuffer)(JNIEnv *env, jclass clazz, jlong handle, jobject src) {
    TensorHandle *tensor = GetTensorFromHandle(env, handle);
    if (tensor == nullptr) return;

    char *src_data_raw = static_cast<char *>(env->GetDirectBufferAddress(src));
    if (!src_data_raw) {
        ThrowException(env, kIllegalArgumentException,
                       "Input ByteBuffer is not a direct buffer");
        return;
    }

    tensor->getMat()->data = src_data_raw;
}

JNIEXPORT jobject JNICALL
NCNNJNI_METHOD2(buffer)(JNIEnv *env, jclass clazz, jlong handler) {
    TensorHandle *tensor = GetTensorFromHandle(env, handler);
    if (tensor->getMat()->data == nullptr) return nullptr;
    if (tensor->getMat()->data == nullptr) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Tensor hasn't been allocated.");
        return nullptr;
    }

    return env->NewDirectByteBuffer(static_cast<void *>(tensor->getMat()->data),
                                    static_cast<jlong>(tensor->getMat()->total() *
                                                       tensor->getMat()->elemsize));
}

JNIEXPORT jintArray JNICALL
NCNNJNI_METHOD2(shape)(JNIEnv *env, jclass clazz, jlong handle) {
    TensorHandle *tensor = GetTensorFromHandle(env, handle);
    if (tensor == nullptr) return nullptr;
//    int num_dims = tensor->getMat()->dims;

    int num_dims = 4;
    int dimsArray[num_dims];
    dimsArray[0] = tensor->getMat()->w;
    dimsArray[1] = tensor->getMat()->h;
    dimsArray[2] = tensor->getMat()->c;
    dimsArray[3] = tensor->getMat()->dims;

    jintArray result = env->NewIntArray(num_dims);
    env->SetIntArrayRegion(result, 0, num_dims, dimsArray);
    return result;
}

JNIEXPORT jint JNICALL
NCNNJNI_METHOD2(numBytes)(JNIEnv *env, jclass clazz, jlong handle) {
    TensorHandle *tensor = GetTensorFromHandle(env, handle);
    if (tensor == nullptr) return 0;
    return static_cast<jint>(tensor->getMat()->total() * tensor->getMat()->elemsize);
}

size_t ElementByteSize(NcnnType data_type) {
    // The code in this file makes the assumption that the
    // TensorFlow TF_DataTypes and the Java primitive types
    // have the same byte sizes. Validate that:
    switch (data_type) {
        case kNcnnFloat32:
//            static_assert(sizeof(jfloat) == 4,
//                          "Interal error: Java float not compatible with "
//                          "kNcnnFloat");
            return 4;
        case kNcnnInt32:
//            static_assert(sizeof(jint) == 4,
//                          "Interal error: Java int not compatible with kNcnnInt");
            return 4;
        case kNcnnUInt8:
//            static_assert(sizeof(jbyte) == 1,
//                          "Interal error: Java byte not compatible with "
//                          "kNcnnUInt8");
            return 1;
        case kNcnnInt64:
//            static_assert(sizeof(jlong) == 8,
//                          "Interal error: Java long not compatible with "
//                          "kNcnnInt64");
            return 8;
        default:
            return 0;
    }
}

size_t ReadOneDimensionalArray(JNIEnv *env, NcnnType data_type,
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
        case kNcnnFloat32: {
            jfloatArray float_array = static_cast<jfloatArray>(dst);
            env->SetFloatArrayRegion(float_array, 0, len,
                                     static_cast<const jfloat *>(src));
            return size;
        }
        case kNcnnInt32: {
            jintArray int_array = static_cast<jintArray>(dst);
            env->SetIntArrayRegion(int_array, 0, len, static_cast<const jint *>(src));
            return size;
        }
        case kNcnnInt64: {
            jlongArray long_array = static_cast<jlongArray>(dst);
            env->SetLongArrayRegion(long_array, 0, len,
                                    static_cast<const jlong *>(src));
            return size;
        }
        case kNcnnUInt8: {
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

size_t ReadMultiDimensionalArray(JNIEnv *env, NcnnType data_type, char *src,
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
NCNNJNI_METHOD2(readMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler, jobject value) {
    TensorHandle *tensor = GetTensorFromHandle(env, handler);
    if (tensor->getMat()->dims == 0) {
        // dims 0
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Cannot copy empty/scalar Tensors.");
        return;
    }

    if (tensor->getDataType() == NcnnType::kNcnnString) {
        //
    } else {
        ReadMultiDimensionalArray(env, tensor->getDataType(), (char *) tensor->getMat()->data,
                                  tensor->getMat()->total() * tensor->getMat()->elemsize,
                                  tensor->getMat()->dims,
                                  static_cast<jarray>(value));
    }
}

size_t WriteOneDimensionalArray(JNIEnv *env, jobject object, NcnnType type,
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
        case kNcnnFloat32: {
            jfloatArray float_array = static_cast<jfloatArray>(array);
            jfloat *float_dst = static_cast<jfloat *>(dst);
            env->GetFloatArrayRegion(float_array, 0, num_elements, float_dst);
            return to_copy;
        }
        case kNcnnInt32: {
            jintArray int_array = static_cast<jintArray>(array);
            jint *int_dst = static_cast<jint *>(dst);
            env->GetIntArrayRegion(int_array, 0, num_elements, int_dst);
            return to_copy;
        }
        case kNcnnInt64: {
            jlongArray long_array = static_cast<jlongArray>(array);
            jlong *long_dst = static_cast<jlong *>(dst);
            env->GetLongArrayRegion(long_array, 0, num_elements, long_dst);
            return to_copy;
        }
        case kNcnnUInt8: {
            jbyteArray byte_array = static_cast<jbyteArray>(array);
            jbyte *byte_dst = static_cast<jbyte *>(dst);
            env->GetByteArrayRegion(byte_array, 0, num_elements, byte_dst);
            return to_copy;
        }
        default: {
            ThrowException(env, kUnsupportedOperationException,
                           "DataType error: TensorFlowLite currently supports float "
                           "(32 bits), int (32 bits), byte (8 bits), and long "
                           "(64 bits), support for other types (DataType %d in this "
                           "case) will be added in the future",
                           kNcnnFloat32, type);
            return 0;
        }
    }
}

size_t WriteMultiDimensionalArray(JNIEnv *env, jobject src, NcnnType type,
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
NCNNJNI_METHOD2(writeMultiDimensionalArray)(JNIEnv *env, jclass clazz, jlong handler, jobject src) {
    TensorHandle *tensor = GetTensorFromHandle(env, handler);
    if (tensor == nullptr) return;
    if (tensor->getDataType() != NcnnType::kNcnnString && tensor->getMat()->data == nullptr) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Target Tensor hasn't been allocated.");
        return;
    }

    if (tensor->getMat()->dims == 0) {
        ThrowException(env, kIllegalArgumentException,
                       "Internal error: Cannot copy empty/scalar Tensors.");
        return;
    }

    if (tensor->getDataType() == NcnnType::kNcnnString) {
    } else {
        char *data = (char *) tensor->getMat()->data;
        WriteMultiDimensionalArray(env, src, tensor->getDataType(), tensor->getMat()->dims,
                                   &data, tensor->getMat()->total() * tensor->getMat()->elemsize);
    }
}
