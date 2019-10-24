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

/**
 * @author noctis
 * @since 1.1.0
 */
interface HttpParams {
    /**
     * Common
     */
    String PARAM_APP_ID = "appId";
    String PARAM_MODEL_ID = "modelId";
    String PARAM_DEVICE_SN = "deviceSN";
    String PARAM_DEVICE_TYPE = "deviceType";
    String PARAM_COMMON_LAT = "kLat";
    String PARAM_COMMON_LNG = "kLng";
    String PARAM_TIMESTAMP = "timeStamp";
    String PARAM_SIGN = "sign";

    /**
     * API: upgradeModel
     */
    String PARAM_MODEL_VERSION_CODE = "modelVersionCode";
}
