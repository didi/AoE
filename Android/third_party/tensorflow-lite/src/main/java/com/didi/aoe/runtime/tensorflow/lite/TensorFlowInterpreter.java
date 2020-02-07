package com.didi.aoe.runtime.tensorflow.lite;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.convertor.Convertor;

import java.util.Map;

/**
 * 基于TensorFlow Lite的运行时Interpreter封装，用于单输入，单输出的常见场景。多路输入的场景不要继承这个类，继承
 * 它的父类TensorFlowLiteMultipleInputsOutputsInterpreter，实现preProcessMulti和postProcessMulti即可。
 *
 * @param <TInput>       范型，业务输入数据
 * @param <TOutput>      范型，业务输出数据
 * @param <TModelInput>  范型，模型输入数据
 * @param <TModelOutput> 范型，模型输出数据
 * @author noctis
 */
public abstract class TensorFlowInterpreter<TInput, TOutput, TModelInput, TModelOutput> extends
        TensorFlowMultipleInputsOutputsInterpreter<TInput, TOutput, Object, TModelOutput> implements
        Convertor<TInput, TOutput, TModelInput, TModelOutput> {

    @Nullable
    @Override
    public final Object[] preProcessMulti(@NonNull TInput tInput) {
        Object[] inputs = new Object[1];
        inputs[0] = preProcess(tInput);
        return inputs;
    }

    @Nullable
    @Override
    public final TOutput postProcessMulti(@Nullable Map<Integer, TModelOutput> modelOutput) {
        if (modelOutput != null && !modelOutput.isEmpty()) {
            return postProcess(modelOutput.get(0));
        }
        return null;
    }

}
