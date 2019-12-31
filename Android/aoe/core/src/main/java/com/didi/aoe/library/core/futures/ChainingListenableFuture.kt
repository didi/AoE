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

import com.didi.aoe.library.core.executer.AoeExecutors
import com.didi.aoe.library.core.futures.Futures.Companion.getUninterruptibly
import com.google.common.util.concurrent.ListenableFuture
import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.*

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class ChainingListenableFuture<I, O> constructor(
        private var mFunction: AsyncFunction<in I, out O>,
        private var mInputFuture: ListenableFuture<out I>
) : FutureChain<O>(), Runnable {

    private val mMayInterruptIfRunningChannel: BlockingQueue<Boolean> = LinkedBlockingQueue(1)
    private val mOutputCreated = CountDownLatch(1)

    @Volatile
    var mOutputFuture: ListenableFuture<out O>? = null

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): O? {
        if (!isDone) { // Invoking get on the mInputFuture will ensure our own run()
            // method below is invoked as a listener when mInputFuture sets
            // its value.  Therefore when get() returns we should then see
            // the mOutputFuture be created.
            val inputFuture = mInputFuture
            inputFuture?.get()
            mOutputCreated.await()
            val outputFuture = mOutputFuture
            outputFuture?.get()
        }
        return super.get()
    }

    /**
     * Delegate the get() to the input and output mFutures, in case
     * their implementations defer starting computation until their
     * own get() is invoked.
     */
    @Throws(TimeoutException::class, ExecutionException::class,
            InterruptedException::class)
    override fun get(timeout: Long, unit: TimeUnit): O? {
        var timeout = timeout
        var unit = unit
        if (!isDone) {
            // Use a single time unit so we can decrease mRemaining timeout
            // as we wait for various phases to complete.
            if (unit !== TimeUnit.NANOSECONDS) {
                timeout = TimeUnit.NANOSECONDS.convert(timeout, unit)
                unit = TimeUnit.NANOSECONDS
            }
            // Invoking get on the mInputFuture will ensure our own run()
            // method below is invoked as a listener when mInputFuture sets
            // its value.  Therefore when get() returns we should then see
            // the mOutputFuture be created.
            val inputFuture = mInputFuture
            var start = System.nanoTime()
            inputFuture?.get(timeout, unit)
            timeout -= Math.max(0, System.nanoTime() - start)
            // If our listener was scheduled to run on an executor we may
            // need to wait for our listener to finish running before the
            // mOutputFuture has been constructed by the mFunction.
            start = System.nanoTime()
            if (!mOutputCreated.await(timeout, unit)) {
                throw TimeoutException()
            }
            timeout -= Math.max(0, System.nanoTime() - start)
            // Like above with the mInputFuture, we have a listener on
            // the mOutputFuture that will set our own value when its
            // value is set.  Invoking get will ensure the output can
            // complete and invoke our listener, so that we can later
            // get the mResult.
            val outputFuture = mOutputFuture
            outputFuture?.get(timeout, unit)
        }
        return super.get(timeout, unit)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        /*
         * Our additional cancellation work needs to occur even if
         * !mayInterruptIfRunning, so we can't move it into interruptTask().
         */
        if (super.cancel(mayInterruptIfRunning)) {
            // This should never block since only one thread is allowed to cancel this Future.
            putUninterruptibly(mMayInterruptIfRunningChannel, mayInterruptIfRunning)
            cancel(mInputFuture, mayInterruptIfRunning)
            cancel(mOutputFuture, mayInterruptIfRunning)
            return true
        }
        return false
    }

    private fun cancel(future: Future<*>?,
            mayInterruptIfRunning: Boolean) {
        future?.cancel(mayInterruptIfRunning)
    }

    override fun run() {
        try {
            val sourceResult: I
            try {
                sourceResult = getUninterruptibly(mInputFuture!!)
            } catch (e: CancellationException) {
                // Cancel this future and return.
                // At this point, mInputFuture is cancelled and mOutputFuture doesn't
                // exist, so the value of mayInterruptIfRunning is irrelevant.
                cancel(false)
                return
            } catch (e: ExecutionException) {
                // Set the cause of the exception as this future's exception
                setException(e)
                return
            }
            mOutputFuture = mFunction?.apply(sourceResult)
            val outputFuture: ListenableFuture<out O>? = mOutputFuture
            if (isCancelled) {
                // Handles the case where cancel was called while the mFunction was
                // being applied.
                // There is a gap in cancel(boolean) between calling sync.cancel()
                // and storing the value of mayInterruptIfRunning, so this thread
                // needs to block, waiting for that value.
                outputFuture?.cancel(takeUninterruptibly(mMayInterruptIfRunningChannel))
                mOutputFuture = null
                return
            }
            outputFuture?.addListener(Runnable {
                try { // Here it would have been nice to have had an
                    // UninterruptibleListenableFuture, but we don't want to start a
                    // combinatorial explosion of interfaces, so we have to make do.
                    set(getUninterruptibly(outputFuture))
                } catch (e: CancellationException) { // Cancel this future and return.
                    // At this point, mInputFuture and mOutputFuture are done, so the
                    // value of mayInterruptIfRunning is irrelevant.
                    cancel(false)
                    return@Runnable
                } catch (e: ExecutionException) { // Set the cause of the exception as this future's exception
                    setException(e)
                } finally { // Don't pin inputs beyond completion
                    mOutputFuture = null
                }
            }, AoeExecutors.directExecutor())
        } catch (e: UndeclaredThrowableException) { // Set the cause of the exception as this future's exception
            setException(e)
        } catch (e: Exception) { // This exception is irrelevant in this thread, but useful for the
            // client
            setException(e)
        } catch (e: Error) { // Propagate errors up ASAP - our superclass will rethrow the error
            setException(e)
        } finally { // Don't pin inputs beyond completion
            //mFunction = null
            //mInputFuture = null
            // Allow our get routines to examine mOutputFuture now.
            mOutputCreated.countDown()
        }
    }

    /**
     * Invokes `queue.`[take()][BlockingQueue.take] uninterruptibly.
     */
    private fun <E> takeUninterruptibly(queue: BlockingQueue<E>): E {
        var interrupted = false
        try {
            while (true) {
                interrupted = try {
                    return queue.take()
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

    /**
     * Invokes `queue.`[put(element)][BlockingQueue.put]
     * uninterruptibly.
     */
    private fun <E> putUninterruptibly(queue: BlockingQueue<E>, element: E) {
        var interrupted = false
        try {
            while (true) {
                interrupted = try {
                    queue.put(element)
                    return
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