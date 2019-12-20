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

package com.didi.aoe.runtime.pytorch

import android.content.Context
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor
import com.didi.aoe.library.api.StatusCode
import com.didi.aoe.library.api.convertor.Convertor
import com.didi.aoe.library.api.domain.ModelSource
import com.didi.aoe.library.api.interpreter.InterpreterInitResult
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener
import com.didi.aoe.library.api.interpreter.SingleInterpreterComponent
import com.didi.aoe.library.common.util.FileUtils
import com.didi.aoe.library.logging.LoggerFactory
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author noctis
 * @since 1.1.0
 */
abstract class PyTorchInterpreter<TInput, TOutput> : SingleInterpreterComponent<TInput, TOutput>(),
        Convertor<TInput, TOutput, Tensor, Tensor> {
    private val mLogger = LoggerFactory.getLogger("PyTorch.Interpreter")

    private var mInterpreter: Module? = null

    override fun init(context: Context, options: AoeProcessor.InterpreterComponent.Options?,
            modelOptions: AoeModelOption, listener: OnInterpreterInitListener?) {
        val modelFilePath = modelOptions.modelDir + "_" + modelOptions.version + File.separator + modelOptions.modelName
        val modelAbsolutePath = FileUtils.getFilesDir(context) + File.separator + modelFilePath;

        @ModelSource val modelSource = modelOptions.modelSource
        if (ModelSource.LOCAL == modelSource) {
            // Pytorch 1.3 只支持传了文件路径给Module加载模型，所以本地assets里的模型文件解压拷贝到对应文件夹
            val assetModelFilePath =
                    if (modelOptions.modelDir.isEmpty())
                        modelOptions.modelName
                    else
                        modelOptions.modelDir + File.separator + modelOptions.modelName
            extractAssetsFile(context, assetModelFilePath, modelAbsolutePath)
        }

        val modelFile = File(FileUtils.getFilesDir(context), modelFilePath)
        if (modelFile.exists() && modelFile.length() > 0) {
            mInterpreter = Module.load(modelFile.absolutePath)
            listener?.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_OK))
        } else {
            if (ModelSource.LOCAL == modelSource) {
                listener?.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_LOAD_FAILED))
            } else {
                // 配置为云端模型，本地无文件，返回等待中状态
                listener?.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_DOWNLOAD_WAITING))
            }
        }

    }

    private fun extractAssetsFile(context: Context, sourceAssetFilePath: String, targetFilePath: String) {
        val file = File(targetFilePath)
        if (file.exists() && file.length() > 0) {
            return
        }

        file.parentFile?.mkdirs()

        try {
            context.assets.open(sourceAssetFilePath).use { input ->
                FileOutputStream(targetFilePath).use { fileOut ->
                    input.copyTo(fileOut)
                }
            }
        } catch (e: IOException) {
            mLogger.error("extractAssetsFile $sourceAssetFilePath error", e)
        }

    }

    override fun run(o: TInput): TOutput? {
        if (isReady) {
            val inputTensor = preProcess(o)
            if (inputTensor != null) {
                val outputTensor = mInterpreter?.forward(IValue.from(inputTensor))?.toTensor()
                return postProcess(outputTensor)
            }
        }

        return null
    }

    override fun release() {
        if (mInterpreter != null) {
            mInterpreter?.destroy()
        }
    }

    override fun isReady(): Boolean {
        return mInterpreter != null
    }
}
