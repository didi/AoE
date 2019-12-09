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

package com.didi.aoe.library.api.domain;

import android.support.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.didi.aoe.library.api.domain.ModelSource.CLOUD;
import static com.didi.aoe.library.api.domain.ModelSource.LOCAL;

/**
 * 定义模型数据来源，目前有两种：本地模型，云端加载模型
 * 本地模型，内置于assets内的模型目录下
 * 云端模型，下载到应用外部存储的模型目录下，并按版本号区分
 * @author noctis
 * @since 1.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({LOCAL, CLOUD})
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
public @interface ModelSource {
    String LOCAL = "local";
    String CLOUD = "cloud";
}
