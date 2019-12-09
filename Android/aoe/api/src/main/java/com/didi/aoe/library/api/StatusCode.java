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

package com.didi.aoe.library.api;



import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.didi.aoe.library.api.StatusCode.STATUS_CONFIG_PARSE_ERROR;
import static com.didi.aoe.library.api.StatusCode.STATUS_CONNECTION_WAITTING;
import static com.didi.aoe.library.api.StatusCode.STATUS_INNER_ERROR;
import static com.didi.aoe.library.api.StatusCode.STATUS_MODEL_DOWNLOAD_WAITING;
import static com.didi.aoe.library.api.StatusCode.STATUS_MODEL_LOAD_FAILED;
import static com.didi.aoe.library.api.StatusCode.STATUS_OK;
import static com.didi.aoe.library.api.StatusCode.STATUS_UNDEFINE;

/**
 * 推理翻译器的状态
 *
 * @author noctis
 * @since 1.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({STATUS_UNDEFINE, STATUS_OK, STATUS_CONFIG_PARSE_ERROR, STATUS_CONNECTION_WAITTING, STATUS_MODEL_DOWNLOAD_WAITING, STATUS_MODEL_LOAD_FAILED, STATUS_INNER_ERROR})
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
public @interface StatusCode {
    /**
     * 内部处理异常
     */
    int STATUS_INNER_ERROR = -2;
    /**
     * 初始状态
     */
    int STATUS_UNDEFINE = -1;
    /**
     * 模型加载正常
     */
    int STATUS_OK = 0;
    /**
     * 模型配置加载失败
     */
    int STATUS_CONFIG_PARSE_ERROR = 1;
    /**
     * 使用独立进程，服务初次启动的过程是异步进行的
     */
    int STATUS_CONNECTION_WAITTING = 2;
    /**
     * 无内置模型，需要云端加载完成
     */
    int STATUS_MODEL_DOWNLOAD_WAITING = 3;
    /**
     * 模型加载失败
     */
    int STATUS_MODEL_LOAD_FAILED = 4;
}
