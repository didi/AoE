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

import android.support.annotation.NonNull;

import com.didi.aoe.library.service.pojos.ModelOption;

/**
 * @author noctis
 * @since 1.1.0
 */
public class ModelRequest {
    private final long mAppId;
    private final long mModelId;
    private final String mCurrentModelVersion;
    private final String mIdentifier;
    private final ModelOption mCurrentModelOption;

    public ModelRequest(long appId, @NonNull ModelOption modelOption) {
        this.mAppId = appId;
        mCurrentModelOption = modelOption;
        this.mModelId = modelOption.getModelId();
        this.mCurrentModelVersion = modelOption.getVersion();
        mIdentifier = new StringBuilder().append(mAppId).append("-")
                .append(mModelId).append("-")
                .append(mCurrentModelVersion).toString();
    }

    public long getAppId() {
        return mAppId;
    }

    public long getModelId() {
        return mModelId;
    }

    public String getCurrentModelVersion() {
        return mCurrentModelVersion;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    @NonNull
    protected ModelOption getCurrentModelOption() {
        return mCurrentModelOption;
    }

    @Override
    public String toString() {
        return "ModelRequest{" +
                "AppId='" + mAppId + '\'' +
                ", ModelId='" + mModelId + '\'' +
                ", CurrentModelVersion='" + mCurrentModelVersion + '\'' +
                ", Identifier='" + mIdentifier + '\'' +
                '}';
    }
}
