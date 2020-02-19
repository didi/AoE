/*
 * Copyright 2019-2020 The AoE Authors. All Rights Reserved.
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

package com.didi.aoe.features.squeeze

import android.content.Context
import android.graphics.Bitmap
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor
import com.didi.aoe.library.api.StatusCode
import com.didi.aoe.library.api.domain.ModelSource
import com.didi.aoe.library.api.interpreter.InterpreterInitResult
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener
import com.didi.aoe.library.common.util.FileUtils.getFilesDir
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.library.service.pojos.ModelOption
import com.didi.aoe.runtime.ncnn.Interpreter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * @author noctis
 * @author fire9953
 */
class SqueezeInterpreter :
        AoeProcessor.InterpreterComponent<Bitmap, String?> {
    private val mLogger =
            LoggerFactory.getLogger("SqueezeInterpreter")
    private var squeeze: Interpreter? = null
    private val lableList = mutableListOf<String?>()

    override fun init(context: Context,
            interpreterOptions: AoeProcessor.InterpreterComponent.Options?,
            modelOptions: List<AoeModelOption>,
            listener: OnInterpreterInitListener?) {
        if (modelOptions.size != 1) {
            listener?.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_LOAD_FAILED))
            return
        }
        // 从assets读取后处理用的Label, TODO 根据下发的文件动态读取
        readLableFileOnce(context, "squeeze/synset_words.txt")
        // 加载模型
        val option = modelOptions[0]
        val ncnnOptions = Interpreter.Options()
        if (interpreterOptions != null) {
            ncnnOptions.setNumberThreads(interpreterOptions.numThreads)
        }
        squeeze = Interpreter(ncnnOptions)

        if (option is ModelOption && option.modelSource == ModelSource.CLOUD) {
            val modelDir = option.modelDir + File.separator + option.version
            val modelFile = File(getFilesDir(context), modelDir + File.separator + option.modelName)
            val paramFile = File(getFilesDir(context), modelDir + File.separator + "squeezenet_v1.1.param.bin")
            if (modelFile.exists() && paramFile.exists()) {
                squeeze?.loadModelAndParam(modelFile.absolutePath, paramFile.absolutePath,
                        1, 1, INPUT_BLOB_INDEX, OUTPUT_BLOB_INDEX)
            }
        } else {
            squeeze?.loadModelAndParam(context.assets, option.getModelDir(),
                    option.modelName, "squeezenet_v1.1.param.bin",
                    1, 1, INPUT_BLOB_INDEX, OUTPUT_BLOB_INDEX)
        }

        if (listener != null) {
            if (squeeze != null && squeeze!!.isLoadModelSuccess) {
                mLogger.debug("model loaded success")
                listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_OK))
            } else {
                listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_LOAD_FAILED))
            }
        }
    }

    override fun run(input: Bitmap): String? {
        if (squeeze == null) {
            return null
        }
        val size = input.byteCount
        val bmpBuffer = ByteBuffer.allocate(size)
        input.copyPixelsToBuffer(bmpBuffer)
        val rgba = bmpBuffer.array()
        squeeze?.inputRgba(rgba, input.width, input.height, INPUT_WIDTH,
                INPUT_HEIGHT,
                meanVals, norVals, 0)
        val buffer = ByteBuffer.allocate(4096)
        squeeze?.run(null, buffer)
        buffer.order(ByteOrder.nativeOrder())
        buffer.flip()
        val shape = squeeze!!.getOutputTensor(0).shape()
        var top_class = 0
        var max_score = 0f
        for (i in 0 until shape[0]) {
            val s = buffer.float
            if (s > max_score) {
                top_class = i
                max_score = s
            }
        }
        if (max_score < 0.5) {
            return null
        }
        return String.format(Locale.ENGLISH, "%s = %.3f", lableList[top_class]!!.substring(10),
                max_score)
    }

    override fun release() {
        if (null != squeeze) {
            squeeze!!.close()
            squeeze = null
        }
    }

    //TODO why return false
    override fun isReady(): Boolean {
        return squeeze != null && squeeze!!.isLoadModelSuccess
    }

    private fun readLableFileOnce(context: Context, fileName: String) {
        var inputReader: InputStreamReader? = null
        var bufReader: BufferedReader? = null
        try {
            inputReader = InputStreamReader(context.resources.assets.open(fileName))
            bufReader = BufferedReader(inputReader)
            var line: String?
            while (bufReader.readLine().also { line = it } != null) {
                lableList.add(line)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputReader?.close()
                bufReader?.close()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        }
    }

    companion object {
        private val meanVals = floatArrayOf(104f, 117f, 123f)
        private val norVals: FloatArray? = null
        private const val INPUT_WIDTH = 227
        private const val INPUT_HEIGHT = 227
        private const val INPUT_BLOB_INDEX = 0
        private const val OUTPUT_BLOB_INDEX = 82
    }
}