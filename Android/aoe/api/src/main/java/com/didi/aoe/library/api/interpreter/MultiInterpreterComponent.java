/*
 * Copyright 2019 The AoE Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.aoe.library.api.interpreter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.didi.aoe.library.AoeRuntimeException;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.StatusCode;

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
    final public void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable OnInterpreterInitListener listener) {
        mSubInterpreters = provideSubInterpreters();

        if (mSubInterpreters.size() != modelOptions.size()) {
            throw new AoeRuntimeException("Size of model options no match with interpreters");
        }

        final CountDownLatch latch = new CountDownLatch(mSubInterpreters.size());

        final AtomicInteger statusCode = new AtomicInteger(StatusCode.STATUS_INNER_ERROR);
        for (SingleInterpreterComponent interpreter : mSubInterpreters) {
            interpreter.init(context, modelOptions, new OnInterpreterInitListener() {
                @Override
                public void onInitResult(@NonNull InterpreterInitResult result) {
                    if (StatusCode.STATUS_OK != result.getCode()) {
                        statusCode.set(result.getCode());
                    }

                    latch.countDown();
                }

            });
        }
        try {
            // 最多等待 3s
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onInitResult(InterpreterInitResult.create(statusCode.get()));
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
