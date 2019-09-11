package com.didi.aoe.library.api;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.didi.aoe.library.AoeRuntimeException;

import java.util.List;

/**
 * 多模型翻译组件
 *
 * @param <TInput>
 * @param <TOutput>
 * @author noctis
 */
public abstract class MultiInterpreterComponent<TInput, TOutput> implements AoeProcessor.InterpreterComponent<TInput, TOutput> {
    private List<SingleInterpreterComponent> mSubInterpreters;

    @NonNull
    public abstract List<SingleInterpreterComponent> provideSubInterpreters();

    @Override
    final public boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions) {
        mSubInterpreters = provideSubInterpreters();

        if (mSubInterpreters.size() != modelOptions.size()) {
            throw new AoeRuntimeException("Size of model options no match with interpreters");
        }

        boolean resultOk = true;
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            resultOk = interpreter.init(context, modelOptions);
            if (!resultOk) {
                // 出现舒适化失败
                break;
            }
        }
        return resultOk;
    }

    @Nullable
    @Override
    public TOutput run(@NonNull TInput tInput) {
        Object input = tInput;
        Object result = null;
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            result = interpreter.run(input);
            input = result;
        }

        return (TOutput) result;
    }

    @Override
    public void release() {
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            interpreter.release();
        }
    }

    @Override
    public boolean isReady() {
        boolean resultOk = true;
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            resultOk = interpreter.isReady();
            if (!resultOk) {
                break;
            }
        }
        return resultOk;
    }
}
