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

import android.content.Context
import android.util.Log
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.sign.Signer
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author noctis
 * @since 1.1.0
 */
class HttpManager private constructor(context: Context) {
    private val mLogger = LoggerFactory.getLogger("ModelContract")
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .hostnameVerifier(HttpTrustManager.getHostnameVerifier())
            .build()

    fun performRequest(url: String, bodyMap: Map<String, Any>, callback: Callback) {
        val request = createRequest(url, bodyMap)
        okHttpClient.newCall(request).enqueue(callback)
    }

    @Throws(IOException::class)
    fun performRequest(url: String, bodyMap: Map<String, Any>): Response {
        val request = createRequest(url, bodyMap)
        return okHttpClient.newCall(request).execute()
    }

    private fun createRequest(url: String, bodyMap: Map<String, Any>): Request {
        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val signParamsMap = generalSignedParamsMap(bodyMap)
        mLogger.debug("===fetchModel request:" + signParamsMap.mapValues { it.value } + " appKey:" + AoeService.getInstance().appInfoProvider.appKey)
        return Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, Gson().toJson(signParamsMap)))
                .build()
    }

    /**
     * 构建完整参数（除sign字段）的有序map
     *
     * @param originMap
     * @return
     */
    private fun generalSignedParamsMap(originMap: Map<String, Any>): Map<String, Any> {
        val commomParamsMap = HashMap<String, Any>(originMap.size)
        commomParamsMap.putAll(originMap)
        commomParamsMap[HttpParams.PARAM_DEVICE_SN] = AoeService.getInstance().appInfoProvider.deviceSN
        commomParamsMap[HttpParams.PARAM_DEVICE_TYPE] = AoeService.getInstance().appInfoProvider.deviceType
        commomParamsMap[HttpParams.PARAM_TIMESTAMP] = System.currentTimeMillis()

        val signMap: Map<String, String> = commomParamsMap.mapValues { "${it.value}" }
//       mLogger.debug("${signMap} appKey: ${appInfoProvider.appKey}")
        val sign = Signer.generalSign(signMap, AoeService.getInstance().appInfoProvider.appKey)
//       mLogger.debug("${sign}")
        commomParamsMap[HttpParams.PARAM_SIGN] = sign

        return commomParamsMap
    }

    companion object {

        var instance: HttpManager? = null
            private set

        fun init(context: Context) {
            synchronized(HttpManager::class.java) {
                if (instance == null) {
                    instance = HttpManager(context)
                }
            }
        }
    }

}
