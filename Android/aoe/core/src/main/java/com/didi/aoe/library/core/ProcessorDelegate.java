package com.didi.aoe.library.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.ParcelComponent;

/**
 * AoE 推理执行委托
 *
 * @author noctis
 */
final class ProcessorDelegate implements AoeProcessor {
    private final AoeProcessor processor;
    private final Paser paser;

    public ProcessorDelegate(AoeProcessor processor) {
        this.processor = processor;
        paser = new Paser();
    }

    @Override
    public void setId(String id) {
        processor.setId(id);
    }

    @NonNull
    @Override
    public InterpreterComponent getInterpreterComponent() {
        return processor.getInterpreterComponent();
    }

    @Nullable
    @Override
    public ParcelComponent getParcelComponent() {
        return processor.getParcelComponent();
    }

    @NonNull
    public Paser getPaser() {
        return paser;
    }
}
