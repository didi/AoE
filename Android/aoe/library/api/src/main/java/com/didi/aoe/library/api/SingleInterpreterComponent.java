package com.didi.aoe.library.api;

import android.content.Context;

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
    final public boolean init(Context context, List<AoeModelOption> modelOptions) {
        if (modelOptions.size() != 1) {
            return false;
        }
        return init(context, modelOptions.get(0));
    }

    public abstract boolean init(Context context, AoeModelOption modelOptions);

}
