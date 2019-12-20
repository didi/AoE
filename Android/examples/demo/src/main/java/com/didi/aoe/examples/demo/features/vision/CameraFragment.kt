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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
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
abstract class CameraFragment : Fragment(R.layout.fragment_camera), ImageAnalysis.Analyzer {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    protected var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        cameraProviderFuture = ProcessCameraProvider.getInstance(context!!)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewView = view.findViewById(R.id.preview_view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.camera_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.navigation_switch_lens_facing -> {
                lensFacing =
                        if (CameraSelector.LENS_FACING_BACK == lensFacing) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                cameraProvider?.unbindAll()
                bindCameraUseCases()
            }

            else ->
                throw IllegalStateException("Unsupport action ${item.itemId}")
        }
        return true
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val analyzer = ImageAnalysis.Builder().build()
        analyzer.setAnalyzer(executor, this)
        preview.previewSurfaceProvider = previewView.previewSurfaceProvider
        cameraProvider?.bindToLifecycle(this,
                cameraSelector, preview, analyzer)
    }
}