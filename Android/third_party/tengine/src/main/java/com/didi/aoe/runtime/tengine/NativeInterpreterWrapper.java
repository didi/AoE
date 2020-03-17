package com.didi.aoe.runtime.tengine;

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

    NativeInterpreterWrapper() {
        interpreterHandle = createInterpreter();
    }

    void loadTengineModelFromPath(String tengineModelPath) {
        loadModelSuccess = loadTengineModelFromPath(tengineModelPath, interpreterHandle);
        if (loadModelSuccess) {
            initTensors();
        }
    }

    private void initTensors() {
        if (tensorAllocated) {
            return;
        }

        this.inputTensors = new Tensor[getInputCount(this.interpreterHandle)];
        this.outputTensors = new Tensor[getOutputCount(this.interpreterHandle)];

        allocateTensors(this.interpreterHandle);
        tensorAllocated = true;
    }

    void inputRgbaResizeToBgr(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float[] channelMeanVals, int inputIndex) {
        if (inputIndex >= inputTensors.length) {
            throw new IllegalArgumentException("Input error: Inputs index should small than max.");
        }

        Tensor tensor = getInputTensor(inputIndex);
        tensor.inputRgbaResizeToBgr(rgbaDate, srcWidth, srcHeight, dstWidth, dstHeight, channelMeanVals);
        dataInputed = true;
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
                    tensor.resizeShape(newShape);
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

    private static native long createInterpreter();

    private static native boolean loadTengineModelFromPath(String tengineModelPath, long interpreterHandle);

    private static native int getInputCount(long interpreterHandle);

    private static native int getOutputCount(long interpreterHandle);

    private static native long allocateTensors(long interpreterHandle);

    private static native int run(long interpreterHandle);

    private static native void delete(long interpreterHandle);
}
