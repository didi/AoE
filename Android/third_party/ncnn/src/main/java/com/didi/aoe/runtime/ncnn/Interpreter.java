package com.didi.aoe.runtime.ncnn;

import android.content.res.AssetManager;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fire9953@gmail.com
 */
public final class Interpreter {
    private static final Logger mLogger = LoggerFactory.getLogger("NcnnInterpreter");

    static {
        try {
            System.loadLibrary("aoe_ncnn");
            mLogger.debug("Init success");
        } catch (UnsatisfiedLinkError e) {
            mLogger.info("library not found!");
        }
    }

    private NativeInterpreterWrapper nativeInterpreterWrapper;

    public Interpreter(Interpreter.Options options) {
        nativeInterpreterWrapper = new NativeInterpreterWrapper(options);
    }

    public void loadModelAndParam(AssetManager assetManager, String folderName, String modelName,
            String paramName, int inputCount, int outputCount,
            int inputBlobIndex, int outputBlobIndex) {
        nativeInterpreterWrapper.loadModelAndParam(assetManager, folderName, modelName, paramName,
                inputCount, outputCount, inputBlobIndex, outputBlobIndex);
    }

    public void inputRgba(byte[] rgbaDate, int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                          float[] meanVals, float[] normVals, int inputIndex) {
        this.checkNotClosed();
        nativeInterpreterWrapper.inputRgba(rgbaDate, srcWidth, srcHeight, dstWidth, dstHeight,
                meanVals, normVals, inputIndex);
    }

    /**
     * Runs model inference if the model takes only one input, and provides only one output.
     *
     * @param input
     * @param output
     */
    public void run(Object input, Object output) {
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, output);
        this.runForMultipleInputsOutputs((null == input) ? null : new Object[]{input}, outputs);
    }

    /**
     * Runs model inference if the model takes multiple inputs, or returns multiple outputs.
     *
     * @param inputs  The inputs should be in the same order as inputs of the model
     * @param outputs
     */
    public void runForMultipleInputsOutputs(Object[] inputs, Map<Integer, Object> outputs) {
        this.checkNotClosed();
        nativeInterpreterWrapper.run(inputs, outputs);
    }

    public Tensor getOutputTensor(int outputIndex) {
        this.checkNotClosed();
        return nativeInterpreterWrapper.getOutputTensor(outputIndex);
    }

    public int getInputTensorCount() {
        this.checkNotClosed();
        return this.nativeInterpreterWrapper.getInputTensorCount();
    }

    public Tensor getInputTensor(int inputIndex) {
        this.checkNotClosed();
        return this.nativeInterpreterWrapper.getInputTensor(inputIndex);
    }

    public int getOutputTensorCount() {
        this.checkNotClosed();
        return this.nativeInterpreterWrapper.getOutputTensorCount();
    }

    public void close() {
        if (this.nativeInterpreterWrapper != null) {
            this.nativeInterpreterWrapper.close();
            this.nativeInterpreterWrapper = null;
        }
    }

    public boolean isLoadModelSuccess() {
        this.checkNotClosed();
        return nativeInterpreterWrapper.isLoadModelSuccess();
    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }
    }

    private void checkNotClosed() {
        if (this.nativeInterpreterWrapper == null) {
            throw new IllegalStateException("Internal error: The Interpreter has already been closed.");
        }
    }

    public static class Options {
        int numThreads = 4;
        boolean lightMode = true;

        public Options() {
        }

        public Interpreter.Options setNumberThreads(int numberThreads) {
            this.numThreads = numberThreads;
            return this;
        }

        public Interpreter.Options setLightMode(boolean lightMode) {
            this.lightMode = lightMode;
            return this;
        }
    }
}
