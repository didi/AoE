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

package com.didi.aoe.examples.demo.features

import android.os.Bundle
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.didi.aoe.examples.demo.R
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
abstract class CameraFragment() : Fragment(R.layout.fragment_camera), ImageAnalysis.Analyzer {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(context!!)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(LensFacing.BACK).build()
            val analyzer = ImageAnalysis.Builder().build()
            analyzer.setAnalyzer(executor, this)
            preview.previewSurfaceProvider = previewView.previewSurfaceProvider
            cameraProvider.bindToLifecycle(this,
                    cameraSelector, preview, analyzer)

        }, ContextCompat.getMainExecutor(context))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewView = view.findViewById(R.id.preview_view)
    }

}