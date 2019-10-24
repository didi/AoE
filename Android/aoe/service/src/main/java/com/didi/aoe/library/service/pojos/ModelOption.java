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

package com.didi.aoe.library.service.pojos;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.didi.aoe.library.api.domain.ModelSource;
import com.didi.aoe.library.modeloption.loader.pojos.LocalModelOption;

/**
 * @author noctis
 * @since 1.1.0
 */
public class ModelOption extends LocalModelOption {
    private String source;

    private String runtime;

    private String modelSign;

    private long modelId;

    @NonNull
    @Override
    public String getModelSource() {
        if (ModelSource.CLOUD.equals(source)
                || ModelSource.LOCAL.equals(source)) {
            return source;
        }
        return super.getModelSource();
    }

    public String getRuntime() {
        return runtime;
    }

    public String getSign() {
        return modelSign;
    }

    public long getModelId() {
        return modelId;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (
                getModelId() != 0L
                        && !TextUtils.isEmpty(getVersion())
                        && !TextUtils.isEmpty(getModelSource())
                        && !TextUtils.isEmpty(getSign())
        );
    }
}
