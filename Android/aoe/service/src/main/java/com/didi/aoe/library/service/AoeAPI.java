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
interface AoeAPI {
    String API_BASE_URL = "https://aoe-test.xiaojukeji.com";

    /**
     * 模型升级
     */
    interface ModelUpdate {
        String API_REQUEST_MODEL_UPDATE = API_BASE_URL + "/upgradeModel";
    }

}
