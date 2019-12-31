/*
 * Copyright 2019 The Android Open Source Project.
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

package com.didi.aoe.library.core.futures

import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Future

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class Futures {

    private class CallbackListener<V> internal constructor(val mFuture: Future<V>,
            val mCallback: FutureCallback<in V>) :
            Runnable {
        override fun run() {
            val value: V
            value = try {
                getDone(mFuture)
            } catch (e: ExecutionException) {
                mCallback.onFailure(e.cause!!)
                return
            } catch (e: RuntimeException) {
                mCallback.onFailure(e)
                return
            } catch (e: Error) {
                mCallback.onFailure(e)
                return
            }
            mCallback.onSuccess(value)
        }

        override fun toString(): String {
            return javaClass.simpleName + "," + mCallback
        }

    }

    companion object {
        @JvmStatic
        fun <V> addCallback(
                future: ListenableFuture<V>,
                callback: FutureCallback<in V>,
                executor: Executor) {
            future.addListener(CallbackListener<V>(future, callback), executor)
        }

        @JvmStatic
        @Throws(ExecutionException::class)
        fun <V> getDone(future: Future<V>): V {
            check(future.isDone) { "Future was expected to be done, $future" }
            return getUninterruptibly(future)
        }

        @JvmStatic
        @Throws(ExecutionException::class)
        fun <V> getUninterruptibly(future: Future<V>): V {
            var interrupted = false
            try {
                while (true) {
                    interrupted = try {
                        return future.get()
                    } catch (e: InterruptedException) {
                        true
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }
}