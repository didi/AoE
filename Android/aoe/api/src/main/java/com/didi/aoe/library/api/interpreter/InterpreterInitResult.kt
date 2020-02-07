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
package com.didi.aoe.library.api.interpreter

import com.didi.aoe.library.api.StatusCode

/**
 * @author noctis
 * @since 1.1.0
 */
class InterpreterInitResult private constructor(@get:StatusCode
@param:StatusCode var code: Int, var msg: String) {

    override fun toString(): String {
        return "InterpreterInitResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}'
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(@StatusCode code: Int,
                msg: String = generalCodeName(code)): InterpreterInitResult {
            return InterpreterInitResult(code, msg)
        }

        /**
         * 默认按CodeName填充msg字段
         *
         * @param code
         * @return
         */
        private fun generalCodeName(code: Int): String {
            val codeName: String
            codeName = when (code) {
                StatusCode.STATUS_INNER_ERROR -> "STATUS_INNER_ERROR"
                StatusCode.STATUS_UNDEFINE -> "STATUS_UNDEFINE"
                StatusCode.STATUS_OK -> "STATUS_OK"
                StatusCode.STATUS_CONFIG_PARSE_ERROR -> "STATUS_CONFIG_PARSE_ERROR"
                StatusCode.STATUS_CONNECTION_WAITTING -> "STATUS_CONNECTION_WAITTING"
                StatusCode.STATUS_MODEL_DOWNLOAD_WAITING -> "STATUS_MODEL_DOWNLOAD_WAITING"
                StatusCode.STATUS_MODEL_LOAD_FAILED -> "STATUS_MODEL_LOAD_FAILED"
                else -> "UNKNOWN"
            }
            return codeName
        }
    }

}