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

package com.didi.aoe.features.pfld

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import com.didi.aoe.runtime.mnn.MNNImageProcess
import com.didi.aoe.runtime.mnn.MNNInterpreter
import com.didi.aoe.runtime.mnn.MNNNetInstance

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class PFLDInterpreter : MNNInterpreter<Bitmap, Array<Point>>() {
    private val INPUT_WIDTH = 96
    private val INPUT_HEIGHT = 96

    override fun postProcess(modelOutput: MNNNetInstance.Session.Tensor?): Array<Point>? {
        val output: FloatArray? = modelOutput?.floatData
        if (output != null) {
            val result = arrayOfNulls<Point>(98)
            result.forEachIndexed { i, _ -> result[i] = Point(output[i * 2].toInt(), output[i * 2 + 1].toInt()) }
            return result as Array<Point>
        }
        return null
    }

    override fun preProcess(input: Bitmap?): MNNNetInstance.Session.Tensor? {
        /*
         *  convert data to input tensor
         */
        val config = MNNImageProcess.Config()
        // normalization params
        // normalization params
        config.mean = floatArrayOf(123.0f, 123.0f, 123.0f)
        config.normal = floatArrayOf(0.017f, 0.017f, 0.017f)
        // input data format
        // input data format
        config.dest = MNNImageProcess.Format.BGR
        // bitmap transform
        // bitmap transform
        val matrix = Matrix()
        matrix.postScale(INPUT_WIDTH / input!!.width.toFloat(), INPUT_HEIGHT / input.height.toFloat())
        matrix.invert(matrix)

        MNNImageProcess.convertBitmap(input, mInputTensor, config, matrix)

        return mInputTensor
    }
}