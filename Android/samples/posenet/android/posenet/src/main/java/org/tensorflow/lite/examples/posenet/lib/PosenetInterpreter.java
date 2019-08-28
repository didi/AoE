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

package org.tensorflow.lite.examples.posenet.lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.runtime.tensorflow.lite.TensorFlowLiteMultipleInputsOutputsInterpreter;

import java.util.Map;

/**
 * @author noctis
 * @since 1.0.0
 */
public class PosenetInterpreter extends TensorFlowLiteMultipleInputsOutputsInterpreter<Object[], Map<Integer, float[][][][]>, Object[], float[][][][]> {


    @Nullable
    @Override
    public Object[] preProcessMulti(@NonNull Object[] objects) {
        return objects;
    }

    @Nullable
    @Override
    public Map<Integer, float[][][][]> postProcessMulti(@Nullable Map<Integer, float[][][][]> modelOutput) {
        return modelOutput;
    }
}
