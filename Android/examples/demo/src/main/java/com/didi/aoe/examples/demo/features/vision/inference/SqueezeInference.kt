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
import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.didi.aoe.examples.demo.R
import com.didi.aoe.extensions.support.image.AoeSupport
import com.didi.aoe.features.squeeze.SqueezeInterpreter
import com.didi.aoe.features.squeeze.extension.SqueezeModelLoaderImpl
import com.didi.aoe.library.api.domain.Device
import com.didi.aoe.library.core.AoeClient
import com.noctis.cameraview.frame.Frame
import com.noctis.cameraview.size.Size
import java.nio.ByteBuffer


/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class SqueezeInference(context: Context, device: Device, numThreads: Int) : Inference(context, device, numThreads) {
    private var viewHolder: SqueezeViewHolder? = null
    private val aoeClient = AoeClient(context, "squeeze",
            AoeClient.Options()
                    .setModelOptionLoader(SqueezeModelLoaderImpl::class.java)
                    .setInterpreter(SqueezeInterpreter::class.java)
                    .setThreadNum(numThreads)
                    .useRemoteService(false),
            "squeeze")

    override fun process(image: Frame): Any? {
        val rotation: Int = image.getRotation()
        val time: Long = image.getTime()
        val size: Size = image.getSize()
        val format: Int = image.getFormat()

        var data: ByteArray?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && image.dataClass == Image::class.java) {
            //if (image.dataClass == Image::class.java) {
            val i: Image = image.getData()
            data = i.planes[0].buffer.toByteArray()

            //}
        } else {
            data = image.getData()
        }

        val argb = AoeSupport.convertNV21ToARGB8888(data, size.width, size.height)
        val bp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        bp.copyPixelsFromBuffer(ByteBuffer.wrap(argb))
        return aoeClient.process(Bitmap.createScaledBitmap(bp, 227, 227, false))
    }

    override fun createView(): View? {
        val v = LayoutInflater.from(context).inflate(R.layout.overlay_squeeze, null)
        viewHolder = SqueezeViewHolder(v)
        return v
    }

    override fun bindView(result: Any?) {
        if (result != null && viewHolder != null) {
            viewHolder!!.detectItemView.text = result.toString()
        }
    }

    override fun release() {
        aoeClient.release()
    }

    override fun isRunning(): Boolean {
        return aoeClient.isRunning
    }

    class SqueezeViewHolder(itemView: View) : ViewHolder(itemView) {
        val detectItemView: TextView = itemView.findViewById(R.id.detected_item)


    }
}