/*
 * Copyright 2019 The AoE Authors. All Rights Reserved.
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

package com.didi.aoe.library.api

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */

interface Component

/**
 * 序列化组件，用于跨进程通信的对象序列化与反序列化
 *
 */
interface ParcelComponent : Component {
    /**
     * 对象序列化为字节数组
     *
     * @param obj 待序列化对象，依赖序列化方案
     * @return 序列化数据
     */
    fun obj2Byte(obj: Any): ByteArray?

    /**
     * 字节数组反序列化为对象实例
     *
     * @param bytes 反序列化需要的字节数组信息
     * @return 数据对象实例
     */
    fun byte2Obj(bytes: ByteArray): Any?
}