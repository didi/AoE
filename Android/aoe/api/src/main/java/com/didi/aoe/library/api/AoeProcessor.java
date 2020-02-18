/*
 * Copyright 2019 The AoE Authors. All Rights Reserved.
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

package com.didi.aoe.library.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.lang.AoeIOException;

import java.util.List;

/**
 * AoE 推理处理器
 *
 * @author noctis
 */
public interface AoeProcessor {
    /**
     * 跨进程通信需要配对服务委托者，每个Client应指定唯一ID
     *
     * @param id ClientId
     */
    void setId(String id);

    @NonNull
    InterpreterComponent getInterpreterComponent();

    @Nullable
    ParcelComponent getParcelComponent();

    /**
     * 模型配置加载组件
     * <p>
     */
    @FunctionalInterface
    interface ModelOptionLoaderComponent extends Component {
        AoeModelOption load(@NonNull Context context, @NonNull String modelDir) throws AoeIOException;
    }

    /**
     * 模型翻译组件
     */
    interface InterpreterComponent<TInput, TOutput> extends Component {
        /**
         * 初始化，推理框架加载模型资源
         *
         * @param context      上下文，用与服务绑定
         * @param modelOptions 模型配置列表
         * @return 推理框架加载
         */
        void init(@NonNull Context context,
                @Nullable InterpreterComponent.Options interpreterOptions,
                @NonNull List<AoeModelOption> modelOptions,
                @Nullable OnInterpreterInitListener listener);

        /**
         * 执行推理操作
         *
         * @param input 业务输入数据
         * @return 业务输出数据
         */
        @Nullable
        TOutput run(@NonNull TInput input);

        /**
         * 释放资源
         */
        void release();

        /**
         * 模型是否正确加载完成
         *
         * @return true，模型正确加载
         */
        boolean isReady();

        /**
         * Interpreter 配置项
         */
        class Options {
            private int numThreads = -1;

            public Options() {
            }

            public Options setNumThreads(int numThreads) {
                this.numThreads = numThreads;
                return this;
            }

            public int getNumThreads() {
                return numThreads;
            }
        }
    }

}
