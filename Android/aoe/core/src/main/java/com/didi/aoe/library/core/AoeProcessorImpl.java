package com.didi.aoe.library.core;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.List;

/**
 * AoeProcessor 的具体实现
 *
 * @author noctis
 */
class AoeProcessorImpl implements AoeProcessor,
        AoeProcessor.InterpreterComponent<Object, Object>,
        AoeProcessor.ParcelComponent {
    private final Logger mLogger = LoggerFactory.getLogger("AoeProcessor");

    private final AbsProcessorWrapper wrapper;

    public AoeProcessorImpl(@NonNull Context context, AoeClient.Options options) {
        if (options.useRemoteService) {
            wrapper = new RemoteProcessorWrapper(context, options);
        } else {
            wrapper = new NativeProcessorWrapper(context, options);
        }
    }

    @Override
    public void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable OnInterpreterInitListener listener) {
        wrapper.init(context, modelOptions, listener);
    }

    @Override
    public Object run(@NonNull Object input) {
        return wrapper.run(input);
    }

    @Override
    public void release() {
        mLogger.debug("release");
        wrapper.release();
    }

    @Override
    public void setId(String id) {
        wrapper.setId(id);
    }

    @NonNull
    @Override
    public InterpreterComponent getInterpreterComponent() {
        return wrapper;
    }

    @NonNull
    @Override
    public ParcelComponent getParcelComponent() {
        return wrapper;
    }

    @Override
    public boolean isReady() {
        return wrapper.isReady();
    }

    @Override
    public byte[] obj2Byte(@NonNull Object obj) {
        return wrapper.obj2Byte(obj);
    }

    @Override
    public Object byte2Obj(@NonNull byte[] bytes) {
        return wrapper.byte2Obj(bytes);
    }
}
