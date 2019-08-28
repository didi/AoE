/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.posenet.lib

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.didi.aoe.extensions.parcel.kryo.KryoParcelImpl
import com.didi.aoe.library.core.AoeClient
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

enum class BodyPart {
  NOSE,
  LEFT_EYE,
  RIGHT_EYE,
  LEFT_EAR,
  RIGHT_EAR,
  LEFT_SHOULDER,
  RIGHT_SHOULDER,
  LEFT_ELBOW,
  RIGHT_ELBOW,
  LEFT_WRIST,
  RIGHT_WRIST,
  LEFT_HIP,
  RIGHT_HIP,
  LEFT_KNEE,
  RIGHT_KNEE,
  LEFT_ANKLE,
  RIGHT_ANKLE
}

class Position {
  var x: Int = 0
  var y: Int = 0
}

class KeyPoint {
  var bodyPart: BodyPart = BodyPart.NOSE
  var position: Position = Position()
  var score: Float = 0.0f
}

class Person {
  var keyPoints = listOf<KeyPoint>()
  var score: Float = 0.0f
}

class Posenet(context: Context) {
  private var aoeClient: AoeClient? = null
  private var useRemoteService : Boolean = false

  init {
    aoeClient = AoeClient(
      context,
      "posenet",
      AoeClient.Options()
        .setInterpreter(PosenetInterpreter::class.java)
        .setParceler(KryoParcelImpl::class.java)
        .useRemoteService(useRemoteService),
      "posenet"
    )
    aoeClient!!.init()
  }

  /** Returns value within [0,1].   */
  private fun sigmoid(x: Float): Float {
    return (1.0f / (1.0f + exp(-x)))
  }

  /**
   * Scale the image to a byteBuffer of [-1,1] values.
   */
  private fun initInputArray(bitmap: Bitmap): ByteBuffer {
    val bytesPerChannel = 4
    val inputChannels = 3
    val batchSize = 1

    val inputBuffer =
      if (useRemoteService)
        ByteBuffer.allocate(batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannels)
      else
        ByteBuffer.allocateDirect(batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannels)

    inputBuffer.order(ByteOrder.nativeOrder())
    inputBuffer.rewind()

    val mean = 128.0f
    val std = 128.0f
    for (row in 0 until bitmap.height) {
      for (col in 0 until bitmap.width) {
        val pixelValue = bitmap.getPixel(col, row)
        inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std)
        inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)
        inputBuffer.putFloat(((pixelValue and 0xFF) - mean) / std)
      }
    }
    return inputBuffer
  }

  /** Preload and memory map the model file, returning a MappedByteBuffer containing the model. */
  private fun loadModelFile(path: String, context: Context): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(path)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    return inputStream.channel.map(
      FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength
    )
  }

  /**
   * Estimates the pose for a single person.
   * args:
   *      bitmap: image bitmap of frame that should be processed
   * returns:
   *      person: a Person object containing data about keypoint locations and confidence scores
   */
  fun estimateSinglePose(bitmap: Bitmap): Person {
    var t1: Long = SystemClock.elapsedRealtimeNanos()
    val inputArray = arrayOf(initInputArray(bitmap))
    var t2: Long = SystemClock.elapsedRealtimeNanos()
    Log.i("posenet", String.format("Scaling to [-1,1] took %.2f ms", 1.0f * (t2 - t1) / 1_000_000))

    t1 = SystemClock.elapsedRealtimeNanos()

    var outputMap = aoeClient?.process(inputArray)

    if (outputMap == null) {
      return Person()
    }

    outputMap as Map<Int, Any>

    t2 = SystemClock.elapsedRealtimeNanos()
    Log.i("posenet", String.format("Interpreter took %.2f ms", 1.0f * (t2 - t1) / 1_000_000))

    val heatmaps = outputMap[0] as Array<Array<Array<FloatArray>>>
    val offsets = outputMap[1] as Array<Array<Array<FloatArray>>>

    val height = heatmaps[0].size
    val width = heatmaps[0][0].size
    val numKeypoints = heatmaps[0][0][0].size

    // Finds the (row, col) locations of where the keypoints are most likely to be.
    val keypointPositions = Array(numKeypoints) { Pair(0, 0) }
    for (keypoint in 0 until numKeypoints) {
      var maxVal = heatmaps[0][0][0][keypoint]
      var maxRow = 0
      var maxCol = 0
      for (row in 0 until height) {
        for (col in 0 until width) {
          heatmaps[0][row][col][keypoint] = sigmoid(heatmaps[0][row][col][keypoint])
          if (heatmaps[0][row][col][keypoint] > maxVal) {
            maxVal = heatmaps[0][row][col][keypoint]
            maxRow = row
            maxCol = col
          }
        }
      }
      keypointPositions[keypoint] = Pair(maxRow, maxCol)
    }

    // Calculating the x and y coordinates of the keypoints with offset adjustment.
    val xCoords = IntArray(numKeypoints)
    val yCoords = IntArray(numKeypoints)
    val confidenceScores = FloatArray(numKeypoints)
    keypointPositions.forEachIndexed { idx, position ->
      val positionY = keypointPositions[idx].first
      val positionX = keypointPositions[idx].second
      yCoords[idx] = (
        position.first / (height - 1).toFloat() * bitmap.height +
          offsets[0][positionY][positionX][idx]
        ).toInt()
      xCoords[idx] = (
        position.second / (width - 1).toFloat() * bitmap.width +
          offsets[0][positionY]
          [positionX][idx + numKeypoints]
        ).toInt()
      confidenceScores[idx] = heatmaps[0][positionY][positionX][idx]
    }

    val person = Person()
    val keypointList = Array(numKeypoints) { KeyPoint() }
    var totalScore = 0.0f
    enumValues<BodyPart>().forEachIndexed { idx, it ->
      keypointList[idx].bodyPart = it
      keypointList[idx].position.x = xCoords[idx]
      keypointList[idx].position.y = yCoords[idx]
      keypointList[idx].score = confidenceScores[idx]
      totalScore += confidenceScores[idx]
    }

    person.keyPoints = keypointList.toList()
    person.score = totalScore / numKeypoints

    return person
  }
}
