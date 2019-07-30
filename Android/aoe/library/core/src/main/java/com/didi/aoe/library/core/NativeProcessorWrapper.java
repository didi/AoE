package com.didi.aoe.library.core;

import android.content.Context;
import android.support.annotation.NonNull;

import com.didi.aoe.library.api.AoeModelOption;

import java.util.List;

/**
 * @author noctis
 */
final class NativeProcessorWrapper extends AbsProcessorWrapper {
    private final InterpreterComponent mInterpreter;
    private final ParcelComponent mParceler;

    public NativeProcessorWrapper(@NonNull Context context, AoeClient.Options options) {
        super(context, options);
        mInterpreter = ComponentProvider.getInterpreter(options.interpreterClassName);
        mParceler = ComponentProvider.getParceler(options.parcelerClassName);
    }

    @Override
    public boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions) {
        return mInterpreter.init(context, modelOptions);
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
