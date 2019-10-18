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

package com.didi.aoe.library.api.interpreter;

import com.didi.aoe.library.api.StatusCode;

import static com.didi.aoe.library.api.StatusCode.STATUS_CONFIG_PARSE_ERROR;
import static com.didi.aoe.library.api.StatusCode.STATUS_CONNECTION_WAITTING;
import static com.didi.aoe.library.api.StatusCode.STATUS_INNER_ERROR;
import static com.didi.aoe.library.api.StatusCode.STATUS_MODEL_DOWNLOAD_WAITING;
import static com.didi.aoe.library.api.StatusCode.STATUS_MODEL_LOAD_FAILED;
import static com.didi.aoe.library.api.StatusCode.STATUS_OK;
import static com.didi.aoe.library.api.StatusCode.STATUS_UNDEFINE;

/**
 * @author noctis
 * @since 1.1.0
 */
public final class InterpreterInitResult {
    private int code;

    private String msg;

    private InterpreterInitResult(@StatusCode int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @StatusCode
    public int getCode() {
        return code;
    }

    public void setCode(@StatusCode int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static InterpreterInitResult create(@StatusCode int code) {
        return create(code, generalCodeName(code));
    }

    public static InterpreterInitResult create(@StatusCode int code, String msg) {
        return new InterpreterInitResult(code, msg);
    }

    @Override
    public String toString() {
        return "InterpreterInitResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

    /**
     * 默认按CodeName填充msg字段
     *
     * @param code
     * @return
     */
    private static String generalCodeName(int code) {
        String codeName;
        switch (code) {
            case STATUS_INNER_ERROR:
                codeName = "STATUS_INNER_ERROR";
                break;
            case STATUS_UNDEFINE:
                codeName = "STATUS_UNDEFINE";
                break;
            case STATUS_OK:
                codeName = "STATUS_OK";
                break;
            case STATUS_CONFIG_PARSE_ERROR:
                codeName = "STATUS_CONFIG_PARSE_ERROR";
                break;
            case STATUS_CONNECTION_WAITTING:
                codeName = "STATUS_CONNECTION_WAITTING";
                break;
            case STATUS_MODEL_DOWNLOAD_WAITING:
                codeName = "STATUS_MODEL_DOWNLOAD_WAITING";
                break;
            case STATUS_MODEL_LOAD_FAILED:
                codeName = "STATUS_MODEL_LOAD_FAILED";
                break;
            default:
                codeName = "UNKNOWN";
                break;
        }
        return codeName;
    }


}
