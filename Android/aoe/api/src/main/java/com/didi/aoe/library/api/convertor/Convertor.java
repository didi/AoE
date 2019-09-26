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

package com.didi.aoe.library.api.convertor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 数据预处理、后处理转换器接口，单输入单输出
 *
 * @author noctis
 * @since 1.1.0
 */
public interface Convertor<TInput, TOutput, TModelInput, TModelOutput> {
    /**
     * 数据预处理，将输入数据转换成模型输入数据
     *
     * @param input 业务输入数据
     * @return 模型输入数据
     */
    @Nullable
    TModelInput preProcess(@NonNull TInput input);

    /**
     * 数据后处理，将模型输出数据转换成业务输出数据
     *
     * @param modelOutput 模型输出数据
     * @return 业务输出数据
     */
    @Nullable
    TOutput postProcess(@Nullable TModelOutput modelOutput);
}
