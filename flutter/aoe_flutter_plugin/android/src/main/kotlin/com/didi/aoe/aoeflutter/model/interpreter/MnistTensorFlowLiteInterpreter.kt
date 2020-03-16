package com.example.aoe_flutter.interpreter

import com.didi.aoe.runtime.tensorflow.lite.TensorFlowInterpreter
import java.nio.ByteBuffer
import android.util.Log
import kotlin.math.roundToInt

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class MnistTensorFlowLiteInterpreter : TensorFlowInterpreter<ByteArray, List<Int>?, FloatArray, Array<FloatArray>>() {
    private val TAG = "MnistInterpreter"
    override fun postProcess(modelOutput: Array<FloatArray>?): List<Int>? {

        if (modelOutput != null && modelOutput.size == 1) {
            val result = modelOutput[0].map {it.roundToInt()}
            Log.d(TAG, "postProcess ${result.toTypedArray().contentToString()}")
            return result
            //return modelOutput[0].indexOf(modelOutput[0].max()!!)
            //
            //
            //for (i in modelOutput[0].indices) {
            //    if (modelOutput[0][i].compareTo(1f) == 0) {
            //        return i
            //    }
            //}
        }

        return null
    }

    override fun preProcess(input: ByteArray?): FloatArray? {
        //Log.d(TAG, "preProcess ${input?.contentToString()} ${input?.size}")
        if (input == null) {
            return null
        }
        val r = FloatArray(input.size / 4)
        for (i in input.indices step 4) {
            r[i / 4] = Float.fromBits(input[i].toInt() or
                    input[i + 1].toInt() shl 8 or
                    input[i + 2].toInt() shl 16 or
                    input[i + 3].toInt() shl 24)
        }
        return r
    }
}