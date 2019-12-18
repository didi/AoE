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

package com.didi.aoe.examples.demo.features.seesawfacenet

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.camera.core.ImageProxy
import com.didi.aoe.examples.demo.features.CameraFragment
import com.didi.aoe.extensions.support.image.AoeSupport
import com.didi.aoe.library.core.AoeClient

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class SeesawCameraFragment : CameraFragment() {
    private var mClient: AoeClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //mClient = AoeClient(context!!, "pfld-mnn",
        //        AoeClient.Options()
        //                .useRemoteService(false),
        //        "pfld")
        //mClient?.init(object : AoeClient.OnInitListener() {
        //    override fun onSuccess() {
        //        super.onSuccess()
        //        Log.d("pfld", "init success")
        //    }
        //
        //    override fun onFailed(code: Int, msg: String?) {
        //        super.onFailed(code, msg)
        //        Log.d("pfld", "init code $code")
        //    }
        //})
    }

    override fun onDestroy() {
        super.onDestroy()
        mClient?.release()
    }

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        Log.d("pfld", "analyze $rotationDegrees ${image.width} * ${image.height}")
        val yuvData = image.planes[0].buffer.toByteArray()
        val rgbData = AoeSupport.convertNV21ToARGB8888(yuvData, image.width, image.height)
        val bitmap = BitmapFactory.decodeByteArray(rgbData, 0, rgbData.size)
        val result = mClient?.process(bitmap)

        Log.d("pfld", "analyze $result")
    }
}