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

package com.didi.aoe.library.service

/**
 * AoE 服务需用户主动提供相关基础信息
 *
 * @author noctis
 * @since 1.1.0
 */
interface AoeDataProvider {

    /**
     * 当前定位纬度
     *
     * @return
     */
    fun latitude(): Double

    /**
     * 经度
     *
     * @return
     */
    fun longitude(): Double
}
