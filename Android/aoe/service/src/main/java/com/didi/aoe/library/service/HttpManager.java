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

package com.didi.aoe.library.service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author noctis
 * @since 1.1.0
 */
public class HttpManager {
    private static final Logger mLogger = LoggerFactory.getLogger("HttpManager");

    private static HttpManager instance;

    private OkHttpClient okHttpClient;

    private final AppInfoProvider appInfoProvider;

    private HttpManager(@NonNull Context context) {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .sslSocketFactory(HttpTrustManager.getSSLSocketFactory(), new HttpTrustManager())
                .hostnameVerifier(HttpTrustManager.getHostnameVerifier())
                .build();

        appInfoProvider = new AppInfoProvider(context);
    }

    public static void init(Context context) {
        synchronized (HttpManager.class) {
            if (instance == null) {
                instance = new HttpManager(context);
            }
        }
    }

    public static HttpManager getInstance() {
        return instance;
    }

    public void performRequest(@NonNull String url, @NonNull Map<String, Object> bodyMap, @NonNull Callback callback) {
        Request request = createRequest(url, bodyMap);
        okHttpClient.newCall(request).enqueue(callback);
    }

    public Response performRequest(@NonNull String url, @NonNull Map<String, Object> bodyMap) throws IOException {
        Request request = createRequest(url, bodyMap);
        return okHttpClient.newCall(request).execute();
    }

    @NonNull
    private Request createRequest(@NonNull String url, @NonNull Map<String, Object> bodyMap) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        Map<String, Object> signParamsMap = generalSignedParamsMap(bodyMap);

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, new Gson().toJson(signParamsMap)))
                .build();
    }

    /**
     * 构建完整参数（除sign字段）的有序map
     *
     * @param originMap
     * @return
     */
    @NonNull
    private Map<String, Object> generalSignedParamsMap(Map<String, Object> originMap) {
        Map<String, Object> commomParamsMap = new HashMap<>(originMap.size());
        commomParamsMap.putAll(originMap);
        commomParamsMap.put(HttpParams.PARAM_DEVICE_SN, appInfoProvider.getDeviceSN());
        commomParamsMap.put(HttpParams.PARAM_DEVICE_TYPE, appInfoProvider.getDeviceType());

        long timestamp = System.currentTimeMillis();
        commomParamsMap.put(HttpParams.PARAM_TIMESTAMP, timestamp);

        // TODO sign the input map

        return commomParamsMap;
    }

}
