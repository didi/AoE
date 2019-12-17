package com.didi.aoe.features.mnist;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.didi.aoe.runtime.tensorflow.lite.TensorFlowLiteInterpreter;

/**
 * @author noctis
 */
public class MnistInterpreter extends TensorFlowLiteInterpreter<float[], Integer, float[], float[][]> {

    @Nullable
    @Override
    public float[] preProcess(@NonNull float[] input) {
        return input;
    }

    @Nullable
    @Override
    public Integer postProcess(@Nullable float[][] modelOutput) {
        if (modelOutput != null && modelOutput.length == 1) {
            for (int i = 0; i < modelOutput[0].length; i++) {
                if (Float.compare(modelOutput[0][i], 1f) == 0) {
                    return i;
                }
            }
        }

        return null;
    }

}
