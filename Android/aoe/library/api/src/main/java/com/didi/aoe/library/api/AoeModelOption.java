package com.didi.aoe.library.api;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 模型文件配置接口
 *
 * @author noctis
 */
public interface AoeModelOption extends Serializable {

    /**
     * 模型文件文件夹路径
     *
     * @return 文件夹路径
     */
    @NonNull
    String getModelDir();

    /**
     * 模型文件名
     *
     * @return 模型文件名，不含路径
     */
    @NonNull
    String getModelName();

    /**
     * 模型配置验证
     *
     * @return true，解析字段符合配置字段基本要求
     */
    boolean isValid();
}
