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

package com.didi.aoe.library.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import com.didi.aoe.library.service.pojos.ModelOption;
import com.didi.aoe.library.api.ModelSource;

import org.junit.Test;

/**
 * @author noctis
 * @since 1.1.0
 */
public class ModelContractTest {

    @Test
    public void requestModel() {
    }

    @Test
    public void fetchModel() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        Aoe.init(context, new AoeDataProvider() {
            @Override
            public long appId() {
                return 164;
            }

            @Override
            public double latitude() {
                return 39.92;
            }

            @Override
            public double longitude() {
                return 116.46;
            }
        });
        ModelOption option = new MockModelOption(
                "0.0",
                "mnist",
                "mnist_cnn_keras.tflite",
                "cloud",
                "tensorflow",
                "822d72ce038baef3e40924016d4550bc",
                75
        );
        ModelRequest modelRequest = new ModelRequest(Aoe.getInstance().getDataProvider().appId(), option);
        ModelContract.ModelResult modelResult = ModelContract.fetchModel(context, modelRequest);
    }

    class MockModelOption extends ModelOption {
        private String version;
        private String modelDir;
        private String modelName;
        private String source;
        private String runtime;
        private String modelSign;
        private long modelId;

        public MockModelOption(String version, String modelDir, String modelName, String source, String runtime, String modelSign, long modelId) {
            this.version = version;
            this.modelDir = modelDir;
            this.modelName = modelName;
            this.source = source;
            this.runtime = runtime;
            this.modelSign = modelSign;
            this.modelId = modelId;
        }

        @NonNull
        @Override
        public String getModelSource() {
            return ModelSource.CLOUD;
        }

        @Override
        public String getRuntime() {
            return super.getRuntime();
        }

        @Override
        public String getSign() {
            return modelSign;
        }

        @Override
        public long getModelId() {
            return modelId;
        }

        @Nullable
        @Override
        public String getVersion() {
            return version;
        }

        @NonNull
        @Override
        public String getModelDir() {
            return modelDir;
        }

        @NonNull
        @Override
        public String getModelName() {
            return modelName;
        }
    }
}