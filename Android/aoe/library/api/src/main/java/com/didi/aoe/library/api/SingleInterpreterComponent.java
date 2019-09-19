package com.didi.aoe.library.api;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * 单模型翻译组件
 *
 * @param <TInput>
 * @param <TOutput>
 * @author noctis
 */
public abstract class SingleInterpreterComponent<TInput, TOutput> implements AoeProcessor.InterpreterComponent<TInput, TOutput> {
    @Override
    final public void init(Context context, List<AoeModelOption> modelOptions, @Nullable AoeProcessor.OnInitListener listener) {
        if (modelOptions.size() != 1) {
            listener.onInitResult(AoeProcessor.InitResult.create(AoeProcessor.StatusCode.STATUS_INNER_ERROR));
            return;
        }

        init(context, modelOptions.get(0), listener);
    }

    public abstract void init(Context context, AoeModelOption modelOptions, @Nullable AoeProcessor.OnInitListener listener);

}
