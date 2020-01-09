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

package com.didi.aoe.examples.demo.features.vision.inference

import android.content.Context
import android.view.View
import com.didi.aoe.library.api.domain.Device
import com.noctis.cameraview.frame.Frame
import java.nio.ByteBuffer

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
abstract class Inference constructor(val context: Context, val device: Device, val numThreads: Int) {

    abstract fun process(image: Frame): Any?

    abstract fun createView(): View?

    abstract fun bindView(result: Any?)

    abstract fun release()

    abstract fun isRunning(): Boolean

    protected fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    open class ViewHolder constructor(val itemView: View)
}