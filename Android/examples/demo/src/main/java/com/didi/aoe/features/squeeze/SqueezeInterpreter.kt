package com.didi.aoe.features.squeeze

import android.content.Context
import android.graphics.Bitmap
import com.didi.aoe.features.squeeze.extension.SqueezeModelOption
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor
import com.didi.aoe.library.api.StatusCode
import com.didi.aoe.library.api.interpreter.InterpreterInitResult
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.runtime.ncnn.Interpreter
import java.io.BufferedReader
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
        // 这是后处理用的Label
        readLableFileOnce(context, "squeeze/synset_words.txt")
        // 加载模型
        val option = modelOptions[0]
        if (option is SqueezeModelOption) {
            val modelOption = option
            val ncnnOptions =
                    Interpreter.Options()
            if (interpreterOptions != null) {
                ncnnOptions.setNumberThreads(interpreterOptions.numThreads)
            }
            squeeze = Interpreter(ncnnOptions)
            squeeze!!.loadModelAndParam(context.assets, option.getModelDir(),
                    modelOption.modelFileName, modelOption.modelParamFileName,
                    1, 1, INPUT_BLOB_INDEX, OUTPUT_BLOB_INDEX)
            if (listener != null) {
                if (squeeze!!.isLoadModelSuccess) {
                    mLogger.debug("model loaded success")
                    listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_OK))
                } else {
                    listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_LOAD_FAILED))
                }
            }
            return
        }
        listener?.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_INNER_ERROR))
    }

    override fun run(input: Bitmap): String? {
        if (squeeze == null) {
            return null
        }
        val size = input.byteCount
        val bmpBuffer = ByteBuffer.allocate(size)
        input.copyPixelsToBuffer(bmpBuffer)
        val rgba = bmpBuffer.array()
        squeeze!!.inputRgba(rgba, input.width, input.height, INPUT_WIDTH,
                INPUT_HEIGHT,
                meanVals, norVals, 0)
        val buffer = ByteBuffer.allocate(4096)
        squeeze!!.run(null, buffer)
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