package com.didi.aoe.library.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.didi.aoe.library.AoeRuntimeException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    final public void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable AoeProcessor.OnInitListener listener) {
        mSubInterpreters = provideSubInterpreters();

        if (mSubInterpreters.size() != modelOptions.size()) {
            throw new AoeRuntimeException("Size of model options no match with interpreters");
        }

        final CountDownLatch latch = new CountDownLatch(mSubInterpreters.size());

        final AtomicInteger statusCode = new AtomicInteger(AoeProcessor.StatusCode.STATUS_INNER_ERROR);
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            interpreter.init(context, modelOptions, new AoeProcessor.OnInitListener() {
                @Override
                public void onInitResult(@NonNull AoeProcessor.InitResult result) {
                    if (AoeProcessor.StatusCode.STATUS_OK != result.getCode()) {
                        statusCode.set(result.getCode());
                    }

                    latch.countDown();
                }
            });
        }
        try {
            // 最多等待1s
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onInitResult(AoeProcessor.InitResult.create(statusCode.get()));
        }

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
