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

package com.didi.aoe.library.core.executer

import androidx.concurrent.futures.DirectExecutor
import java.util.concurrent.Executor

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class AoeExecutors private constructor() {
    companion object {

        /** Returns a cached executor that runs tasks directly from the calling thread.  */
        @JvmStatic
        fun directExecutor(): Executor {
            return DirectExecutor.INSTANCE
        }
    }
}