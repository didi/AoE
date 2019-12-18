package com.didi.aoe.library.core;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;

import java.util.List;

/**
 * @author noctis
 */
final class NativeProcessorWrapper extends AbsProcessorWrapper {
    private final InterpreterComponent mInterpreter;
    private final ParcelComponent mParceler;

    public NativeProcessorWrapper(@NonNull Context context, AoeClient.Options options) {
        super(context, options);
        if (options.interpreter != null) {
            mInterpreter = options.interpreter;
        } else {
            mInterpreter = ComponentProvider.getInterpreter(options.interpreterClassName);
        }
        mParceler = ComponentProvider.getParceler(options.parcelerClassName);
    }

    @Override
    public void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable OnInterpreterInitListener listener) {
        mInterpreter.init(context, modelOptions, listener);
    }

    @Override
    public Object run(@NonNull Object input) {
        //noinspection unchecked
        return mInterpreter.run(input);
    }

    @Override
    public void release() {
        mInterpreter.release();
    }

    @NonNull
    @Override
    public InterpreterComponent getInterpreterComponent() {
        return mInterpreter;
    }

    @NonNull
    @Override
    public ParcelComponent getParcelComponent() {
        return mParceler;
    }
}
