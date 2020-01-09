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
import androidx.fragment.app.Fragment
import com.didi.aoe.examples.demo.R
import com.noctis.cameraview.controls.Facing
import com.noctis.cameraview.frame.Frame
import com.noctis.cameraview.frame.FrameProcessor
import kotlinx.android.synthetic.main.fragment_camera.*
import java.util.concurrent.Executors

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
abstract class CameraFragment : Fragment(R.layout.fragment_camera), FrameProcessor {
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preview_view.setLifecycleOwner(viewLifecycleOwner)
        preview_view.addFrameProcessor(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.camera_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.navigation_switch_lens_facing -> {
                //lensFacing =
                //        if (CameraSelector.LENS_FACING_BACK == lensFacing) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                preview_view.facing = if (Facing.BACK == preview_view.facing) Facing.FRONT else Facing.BACK
            }

            else ->
                throw IllegalStateException("Unsupport action ${item.itemId}")
        }
        return true
    }

}