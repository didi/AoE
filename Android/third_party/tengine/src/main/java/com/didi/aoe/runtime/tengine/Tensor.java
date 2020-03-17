package com.didi.aoe.runtime.tengine;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author fire9953@gmail.com
 */
public class Tensor {
    public long nativeHandle;
    private int[] shapeCopy;

    static Tensor fromIndex(long nativeInterpreterHandle, int tensorIndex, boolean isInput) {
        return new Tensor(create(nativeInterpreterHandle, tensorIndex, isInput));
    }

    private Tensor(long nativeHandle) {
        this.nativeHandle = nativeHandle;
        this.shapeCopy = shape(nativeHandle);
    }

    void close() {
//        delete(this.nativeHandle);
        this.nativeHandle = 0L;
    }

    public int[] shape() {
        return shapeCopy;
    }

    public void resizeShape(int[] dims){
        resizeInputShape(dims, nativeHandle);
        refreshShape();
    }

    public void refreshShape() {
        this.shapeCopy = shape(this.nativeHandle);
    }

    void inputRgbaResizeToBgr(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float[] channelMeanVals){
        inputRgbaResizeToBgr(rgbaDate, srcWidth, srcHeight, dstWidth, dstHeight, channelMeanVals, nativeHandle);
    }

    void setTo(Object src) {
        this.throwExceptionIfTypeIsIncompatible(src);
        if (!isByteBuffer(src)) {
            writeMultiDimensionalArray(this.nativeHandle, src);
        } else {
            ByteBuffer srcBuffer = (ByteBuffer) src;
            if (srcBuffer.isDirect() && srcBuffer.order() == ByteOrder.nativeOrder()) {
                writeDirectBuffer(this.nativeHandle, srcBuffer);
            } else {
                this.buffer().put(srcBuffer);
            }

        }
    }

    Object copyTo(Object dst) {
        if (dst instanceof ByteBuffer) {
            ByteBuffer dstByteBuffer = (ByteBuffer) dst;
            ByteBuffer result = this.buffer();
            if (null != result) {
                dstByteBuffer.put(result);
            }
            return dst;
        } else {
            readMultiDimensionalArray(this.nativeHandle, dst);
            return dst;
        }
    }

    int[] getInputShapeIfDifferent(Object input) {
        if (isByteBuffer(input)) {
            return null;
        } else {
            int[] inputShape = computeShapeOf(input);
            return Arrays.equals(this.shapeCopy, inputShape) ? null : inputShape;
        }
    }

    static int[] computeShapeOf(Object o) {
        int size = computeNumDimensions(o);
        int[] dimensions = new int[size];
        fillShape(o, 0, dimensions);
        return dimensions;
    }

    static int computeNumDimensions(Object o) {
        if (o != null && o.getClass().isArray()) {
            if (Array.getLength(o) == 0) {
                throw new IllegalArgumentException("Array lengths cannot be 0.");
            } else {
                return 1 + computeNumDimensions(Array.get(o, 0));
            }
        } else {
            return 0;
        }
    }

    static void fillShape(Object o, int dim, int[] shape) {
        if (shape != null && dim != shape.length) {
            int len = Array.getLength(o);
            if (shape[dim] == 0) {
                shape[dim] = len;
            } else if (shape[dim] != len) {
                throw new IllegalArgumentException(String.format("Mismatched lengths (%d and %d) in dimension %d", shape[dim], len, dim));
            }

            for (int i = 0; i < len; ++i) {
                fillShape(Array.get(o, i), dim + 1, shape);
            }
        }
    }

    private static boolean isByteBuffer(Object o) {
        return o instanceof ByteBuffer;
    }

    public int numBytes() {
        return numBytes(this.nativeHandle);
    }

    private void throwExceptionIfTypeIsIncompatible(Object o) {
        if (isByteBuffer(o)) {
            ByteBuffer oBuffer = (ByteBuffer) o;
            if (oBuffer.capacity() != this.numBytes()) {
                throw new IllegalArgumentException(String.format("Cannot convert between a TensorFlowLite buffer with %d bytes and a ByteBuffer with %d bytes.", this.numBytes(), oBuffer.capacity()));
            }
        } else {
            // TODO
        }
    }

    private ByteBuffer buffer() {
        ByteBuffer buffer = buffer(this.nativeHandle);
        if (null != buffer) {
            buffer.order(ByteOrder.nativeOrder());
        }
        return buffer;
    }

    private static native long create(long interpreterHandle, int tensorIndex, boolean isInput);

    private static native void delete(long handle);

    private static native int inputRgbaResizeToBgr(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float[] channelMeanVals, long handler);

    private static native void writeDirectBuffer(long handle, ByteBuffer src);

    private static native ByteBuffer buffer(long handle);

    private static native void resizeInputShape(int[] dims, long handle);

    private static native int[] shape(long handle);

    private static native int numBytes(long handle);

    private static native void readMultiDimensionalArray(long handle, Object dst);

    private static native void writeMultiDimensionalArray(long handle, Object src);
}
