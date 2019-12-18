/*
 * Copyright 2019 The AoE Authors.
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

package com.didi.aoe.features.pytorch

import android.graphics.Bitmap
import com.didi.aoe.pytorch.PytorchConvertor
import com.didi.aoe.runtime.pytorch.PyTorchInterpreter
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class ClassifierPyTorchInterpreter : PytorchConvertor<Bitmap, String> {
    override fun preProcess(input: Bitmap): Tensor? {
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(input,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
        return inputTensor

    }

    override fun postProcess(modelOutput: Tensor?): String? {
        val scores = modelOutput?.dataAsFloatArray

        // searching for the index with maximum score
        var maxScore = -java.lang.Float.MAX_VALUE
        var maxScoreIdx = -1
        if (scores != null) {
            for (i in scores.indices) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i]
                    maxScoreIdx = i
                }
            }

            return ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx]
        }
        return null
    }


}