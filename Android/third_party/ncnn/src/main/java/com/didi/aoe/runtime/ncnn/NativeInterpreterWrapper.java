package com.didi.aoe.runtime.ncnn;

import android.content.res.AssetManager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author fire9953@gmail.com
 */
class NativeInterpreterWrapper {
    private long interpreterHandle;
    private boolean loadModelSuccess;
    private boolean tensorAllocated;

    private Tensor[] inputTensors;
    private Tensor[] outputTensors;

    private boolean dataInputed;

    NativeInterpreterWrapper(Interpreter.Options options) {
        if (null == options) {
            options = new Interpreter.Options();
        }

        interpreterHandle = createInterpreter(options.lightMode, options.numThreads);
    }


    void loadModelAndParam(AssetManager assetManager, String path, String modelName, String paramName,
                           int inputCount, int outputCount, int inputBlobIndex, int outputBlobIndex) {
        loadModelSuccess = loadParamFromAssets(assetManager, path, paramName, interpreterHandle);
        loadModelSuccess = loadModelFromAssets(assetManager, path, modelName, interpreterHandle);

        checkInitTensors(inputCount, outputCount);
        setBlobIndex(inputBlobIndex, outputBlobIndex, interpreterHandle);
    }

    void inputRgba(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float[] meanVals, float[] normVals, int inputIndex) {
        if (inputIndex >= inputTensors.length) {
            throw new IllegalArgumentException("Input error: Inputs index should small than max.");
        }

        inputRgba(rgbaDate, srcWidth, srcHeight, dstWidth, dstHeight, meanVals, normVals, interpreterHandle, getInputTensor(inputIndex).getNativeHandle());
        dataInputed = true;
    }

    private void checkInitTensors(int inputCount, int outputCount) {
        if (tensorAllocated) {
            return;
        }

        this.inputTensors = new Tensor[inputCount];
        this.outputTensors = new Tensor[outputCount];

        allocateTensors(this.interpreterHandle, inputCount, outputCount);

        tensorAllocated = true;
    }

    Tensor getInputTensor(int index) {
        if (index >= 0 && index < this.inputTensors.length) {
            Tensor inputTensor = this.inputTensors[index];
            if (inputTensor == null) {
                inputTensor = this.inputTensors[index] = Tensor.fromIndex(this.interpreterHandle, index, true);
            }

            return inputTensor;
        } else {
            throw new IllegalArgumentException("Invalid input Tensor index: " + index);
        }
    }

    Tensor getOutputTensor(int index) {
        if (index >= 0 && index < this.outputTensors.length) {
            Tensor outputTensor = this.outputTensors[index];
            if (outputTensor == null) {
                outputTensor = this.outputTensors[index] = Tensor.fromIndex(this.interpreterHandle, index, false);
            }

            return outputTensor;
        } else {
            throw new IllegalArgumentException("Invalid output Tensor index: " + index);
        }
    }

    void close() {
        if (interpreterHandle > 0) {
            delete(interpreterHandle);
            interpreterHandle = 0;
        }

        int i;
        if (null != inputTensors) {
            for (i = 0; i < inputTensors.length; ++i) {
                if (this.inputTensors[i] != null) {
                    this.inputTensors[i].close();
                    this.inputTensors[i] = null;
                }
            }
        }

        if (null != outputTensors) {
            for (i = 0; i < this.outputTensors.length; ++i) {
                if (this.outputTensors[i] != null) {
                    this.outputTensors[i].close();
                    this.outputTensors[i] = null;
                }
            }
        }

        loadModelSuccess = false;
        dataInputed = false;
    }

    void run(Object[] inputs, Map<Integer, Object> outputs) {
        if (!dataInputed && (null == inputs || inputs.length == 0)) {
            throw new IllegalArgumentException("Input error: Inputs should not be null or empty.");
        }

        if (null == outputs || outputs.isEmpty()) {
            throw new IllegalArgumentException("Input error: Outputs should not be null or empty.");
        }

        if (inputs != null && inputs.length != 0) {
            for (int i = 0; i < inputs.length; ++i) {
                Tensor tensor = this.getInputTensor(i);
                int[] newShape = tensor.getInputShapeIfDifferent(inputs[i]);
                if (newShape != null) {
                    this.resizeInput(i, newShape);
                }
            }

            for (int i = 0; i < inputs.length; ++i) {
                this.getInputTensor(i).setTo(inputs[i]);
            }
        }

        int ret = run(interpreterHandle);
        if (ret == 0) {
            for (int i = 0; i < this.outputTensors.length; ++i) {
                this.getOutputTensor(i).refreshShape();
            }

            Iterator iterator = outputs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Object> output = (Map.Entry) iterator.next();
                this.getOutputTensor(output.getKey()).copyTo(output.getValue());
            }
        }
    }

    boolean isLoadModelSuccess() {
        return loadModelSuccess;
    }

    int getInputTensorCount() {
        return this.inputTensors.length;
    }

    int getOutputTensorCount() {
        return this.outputTensors.length;
    }

    void resizeInput(int idx, int[] dims) {

    }

    private static native long createInterpreter(boolean lightMode, int numThreads);

    private static native boolean loadModelFromAssets(AssetManager assetManager, String folderName, String fileName, long interpreterHandle);

    private static native boolean loadParamFromAssets(AssetManager assetManager, String folderName, String fileName, long interpreterHandle);

    private static native void setBlobIndex(int inputBlobIndex, int outputBlobIndex, long interpreterHandle);

    private static native int getInputCount(long interpreterHandle);

    private static native int getOutputCount(long interpreterHandle);

    private static native void inputRgba(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float[] meanVals, float[] normVals, long interpreterHandle, long tensorHandle);

    private static native long allocateTensors(long interpreterHandle, int inputCount, int outputCount);

    private static native int run(long interpreterHandle);

    private static native void delete(long interpreterHandle);
}
