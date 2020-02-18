package com.didi.aoe.library.modeloption.loader.pojos

import android.support.annotation.Keep
import android.text.TextUtils
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.domain.ModelSource
import com.google.gson.annotations.SerializedName

/**
 * AoE默认的模型配置定义
 *
 * @author noctis
 */
@Keep
open class LocalModelOption : AoeModelOption {
    @SerializedName("version")
    private val version: String? = null
    /**
     * 模型文件assets目录路径
     */
    @SerializedName("modelDir")
    private var modelDir: String? = null
    /**
     * 模型文件名(带后缀)
     */
    @SerializedName("modelName")
    private var modelName: String? = null

    constructor() {}
    constructor(modelDir: String?, modelName: String?) {
        this.modelDir = modelDir
        this.modelName = modelName
    }

    override fun getVersion(): String? {
        return version
    }

    override fun getModelDir(): String {
        return modelDir!!
    }

    override fun getModelName(): String {
        return modelName!!
    }

    override fun getModelSource(): String {
        return ModelSource.LOCAL
    }

    override fun isValid(): Boolean {
        return !TextUtils.isEmpty(modelDir) &&
                !TextUtils.isEmpty(modelName)
    }

    override fun toString(): String {
        return "LocalModelOption(version=$version, modelDir=$modelDir, modelName=$modelName)"
    }


}