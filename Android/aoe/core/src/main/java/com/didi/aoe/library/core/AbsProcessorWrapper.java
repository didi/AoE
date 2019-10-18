package com.didi.aoe.library.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.AoeProcessor.InterpreterComponent;

/**
 * @author noctis
 */
abstract class AbsProcessorWrapper implements AoeProcessor,
        InterpreterComponent<Object, Object>,
        AoeProcessor.ParcelComponent {
    final Context mContext;

    AbsProcessorWrapper(@NonNull Context context, @NonNull AoeClient.Options options) {
        mContext = context;
    }

    @NonNull
    @Override
    public InterpreterComponent getInterpreterComponent() {
        //noinspection ConstantConditions
        return null;
    }

    @Nullable
    @Override
    public ParcelComponent getParcelComponent() {
        return null;
    }

    @Override
    public byte[] obj2Byte(@NonNull Object obj) {
        return new byte[0];
    }

    @Override
    public Object byte2Obj(@NonNull byte[] bytes) {
        return null;
    }

    @Override
    public boolean isReady() {
        return getInterpreterComponent().isReady();
    }

    @Override
    public void setId(String id) {
    }

}
