package com.didi.aoe.library.modeloption.loader.pojos;

import android.support.annotation.Keep;
import android.text.TextUtils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.domain.ModelSource;

/**
 * AoE默认的模型配置定义
 *
 * @author noctis
 */
@Keep
public class LocalModelOption implements AoeModelOption {
    private String version;

    /**
     * 模型文件assets目录路径
     */
    private String modelDir;
    /**
     * 模型文件名(带后缀)
     */
    private String modelName;

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

    @NonNull
    @Override
    public String getModelSource() {
        return ModelSource.LOCAL;
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(modelDir) &&
                !TextUtils.isEmpty(modelName);
    }

    @Override
    public String toString() {
        return "LocalModelOption{" +
                "version='" + version + '\'' +
                ", modelDir='" + modelDir + '\'' +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}
