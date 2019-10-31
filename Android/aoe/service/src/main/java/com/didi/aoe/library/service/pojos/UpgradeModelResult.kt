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

package com.didi.aoe.library.service.pojos

import android.text.TextUtils

/**
 * 模型升级查询接口返回data
 *
 * @author noctis
 * @since 1.1.0
 */
data class UpgradeModelResult(
        var versionName: String? = null,
        var url: String? = null,
        var md5: String? = null
) {
    val isValid: Boolean
        get() = (!TextUtils.isEmpty(versionName)
                && !TextUtils.isEmpty(url)
                && !TextUtils.isEmpty(md5))

    override fun toString(): String {
        return "UpgradeModelResult{" +
                "versionName='" + versionName + '\''.toString() +
                ", url='" + url + '\''.toString() +
                ", md5='" + md5 + '\''.toString() +
                '}'.toString()
    }
}
