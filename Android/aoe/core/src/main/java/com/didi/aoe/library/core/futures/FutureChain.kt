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

import androidx.concurrent.futures.CallbackToFutureAdapter
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
open class FutureChain<V> : ListenableFuture<V> {
    private val mDelegate: ListenableFuture<V>
    var mCompleter: CallbackToFutureAdapter.Completer<V>? = null

    constructor() {
        mDelegate = CallbackToFutureAdapter.getFuture { completer ->
            check(mCompleter == null) { "The result can only set once!" }
            mCompleter = completer
            "FutureChain[" + this@FutureChain + "]"
        }
    }

    constructor(future: ListenableFuture<V>) {
        mDelegate = checkNotNull(future) { "Required value was null." }

    }

    override fun addListener(listener: Runnable, executor: Executor) {
        mDelegate.addListener(listener, executor)
    }

    override fun isDone(): Boolean {
        return mDelegate.isDone
    }

    override fun get(): V? {
        return mDelegate.get()
    }

    override fun get(timeout: Long, unit: TimeUnit): V? {
        return mDelegate.get(timeout, unit)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return mDelegate.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return mDelegate.isCancelled
    }

    fun addCallback(callback: FutureCallback<in V?>,
            executor: Executor) {
        Futures.addCallback(this, callback, executor)
    }

    fun set(value: V?): Boolean {
        return mCompleter?.set(value) ?: false
    }

    fun setException(throwable: Throwable): Boolean {
        return mCompleter?.setException(throwable) ?: false
    }

    companion object {
        @JvmStatic
        fun <V> from(future: ListenableFuture<V>): FutureChain<V> {
            return if (future is FutureChain) future else FutureChain(future)
        }
    }


}