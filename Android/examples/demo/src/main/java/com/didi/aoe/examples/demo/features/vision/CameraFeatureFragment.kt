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

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.AdapterView
import androidx.annotation.IntRange
import com.didi.aoe.examples.demo.R
import com.didi.aoe.examples.demo.features.Model
import com.didi.aoe.library.api.domain.Device
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.noctis.cameraview.frame.Frame
import kotlinx.android.synthetic.main.fragment_bottom_sheet_model_config.*
import java.util.*

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
abstract class CameraFeatureFragment : CameraFragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var sheetBehavior: BottomSheetBehavior<View>

    protected var currentDevice = Device.CPU
    protected var currentModel = Model.NCNN_SQUEEZE
    protected var currentNumThreads = 1

    private val mBottomSheetBehavior = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                in arrayOf(BottomSheetBehavior.STATE_SETTLING,
                        BottomSheetBehavior.STATE_COLLAPSED) -> bottom_sheet_arrow.setImageResource(
                        R.drawable.ic_arrow_drop_up_black_24dp)
                BottomSheetBehavior.STATE_EXPANDED -> bottom_sheet_arrow.setImageResource(
                        R.drawable.ic_arrow_drop_down_black_24dp)
                else -> {
                }

            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout)
        gesture_layout.viewTreeObserver.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    gesture_layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                sheetBehavior.peekHeight = gesture_layout.measuredHeight
            }

        })
        gesture_layout.setOnClickListener(this)
        sheetBehavior.isHideable = false
        sheetBehavior.addBottomSheetCallback(mBottomSheetBehavior)

        currentNumThreads = threads.text.trim().toString().toInt()
        minus.setOnClickListener(this)
        plus.setOnClickListener(this)

        model_spinner.onItemSelectedListener = this
        device_spinner.onItemSelectedListener = this
    }

    override fun onDestroyView() {
        sheetBehavior.removeBottomSheetCallback(mBottomSheetBehavior)
        super.onDestroyView()
    }

    protected abstract fun onInferenceConfigurationChanged()

    protected abstract fun runInBackground(image: Frame)

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.minus -> {
                var numThreads = threads.text.trim().toString().toInt()
                if (numThreads <= 1) {
                    return
                }
                numThreads -= 1
                setNumThreads(numThreads)
            }
            R.id.plus -> {
                var numThreads = threads.text.trim().toString().toInt()
                if (numThreads >= 9) {
                    return
                }
                numThreads += 1
                setNumThreads(numThreads)

            }
            R.id.gesture_layout -> sheetBehavior.state =
                    if (BottomSheetBehavior.STATE_COLLAPSED == sheetBehavior.state) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setNumThreads(@IntRange(from = 1, to = 9) numThreads: Int) {
        if (this.currentNumThreads != numThreads) {
            this.currentNumThreads = numThreads
            onInferenceConfigurationChanged()
        }
        threads.text = this.currentNumThreads.toString()

    }

    private fun setModel(model: Model) {
        if (this.currentModel != model) {
            this.currentModel = model
            onInferenceConfigurationChanged()
        }

    }

    private fun setDevice(device: Device) {
        if (this.currentDevice != device) {
            this.currentDevice = device
            onInferenceConfigurationChanged()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            model_spinner -> setModel(
                    Model.valueOf(
                            model_spinner.selectedItem.toString().trim().replace(" ", "_").toUpperCase(Locale.ENGLISH)))
            device_spinner -> setDevice(
                    Device.valueOf(device_spinner.selectedItem.toString().toUpperCase(Locale.ENGLISH)))
        }
    }

    override fun process(frame: Frame) {
        runInBackground(frame)
    }

}