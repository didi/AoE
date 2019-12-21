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

package com.didi.aoe.examples.demo.features.vision

import android.os.Bundle
import android.view.View
import androidx.camera.core.ImageProxy
import com.didi.aoe.examples.demo.features.Model
import com.didi.aoe.examples.demo.features.vision.inference.Inference
import com.didi.aoe.examples.demo.features.vision.inference.SqueezeInference
import com.didi.aoe.library.api.domain.Device
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class InferenceByVideoFragment : CameraFeatureFragment() {
    private var inference: Inference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInferenceConfigurationChanged()
    }

    override fun runInBackground(image: ImageProxy) {
        val result = inference?.process(image)
        CoroutineScope(Dispatchers.Main).launch {
            inference?.bindView(result)
        }
    }

    override fun onInferenceConfigurationChanged() {
        recreateAoeClient(currentModel, currentDevice, currentNumThreads)
        replaceOverlayView(inference?.createView())
    }

    private fun recreateAoeClient(model: Model, device: Device, numThreads: Int) {
        inference?.release()

        inference = when (model) {
            Model.NCNN_SQUEEZE -> SqueezeInference(context!!.applicationContext, device, numThreads)
            else -> SqueezeInference(context!!.applicationContext, device, numThreads)
        }

    }

    private fun replaceOverlayView(overlayView: View?) {
        overlay_container.removeAllViews()
        if (overlayView != null) {
            overlay_container.addView(overlayView)
        }
    }

}