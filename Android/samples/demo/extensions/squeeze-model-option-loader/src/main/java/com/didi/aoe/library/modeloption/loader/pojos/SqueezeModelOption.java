package com.didi.aoe.library.modeloption.loader.pojos;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.didi.aoe.library.api.AoeModelOption;

/**
 * @author noctis
 */
public class SqueezeModelOption implements AoeModelOption {
    private String version;

    /**
     * 模型文件目录路径，目前只支持local模型
     */
    private String modelDir;
    /**
     * 模型文件名，便于拓展多种模型
     */
    private String modelName;

    private String modelFileName;
    private String modelParamFileName;
    private String labelFileName;

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

    public String getVersion() {
        return version;
    }

    public String getModelFileName() {
        return modelFileName;
    }

    public String getModelParamFileName() {
        return modelParamFileName;
    }

    public String getLabelFileName() {
        return labelFileName;
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(modelDir)
                && !TextUtils.isEmpty(modelName)
                && !TextUtils.isEmpty(modelFileName)
                && !TextUtils.isEmpty(modelParamFileName)
                && !TextUtils.isEmpty(labelFileName);
    }

    @NonNull
    @Override
    public String toString() {
        return "SqueezeModelOption{" +
                "version='" + version + '\'' +
                ", modelDir='" + modelDir + '\'' +
                ", modelName='" + modelName + '\'' +
                ", modelFileName='" + modelFileName + '\'' +
                ", modelParamFileName='" + modelParamFileName + '\'' +
                ", labelFileName='" + labelFileName + '\'' +
                '}';
    }

}
