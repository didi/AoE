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

package com.didi.aoe.examples.demo.features.classification

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.didi.aoe.examples.demo.R
import com.didi.aoe.examples.demo.features.BaseFeartureFragment
import com.didi.aoe.examples.demo.utils.debug
import com.didi.aoe.library.core.AoeClient
import java.io.FileNotFoundException


class ClassificationFeartureFragment : BaseFeartureFragment() {
    private var mClassifier: Classifier? = null

    private var infoResult: TextView? = null
    private var imageView: ImageView? = null
    private var yourSelectedImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_classification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoResult = view.findViewById(R.id.tv_result)
        infoResult?.movementMethod = ScrollingMovementMethod.getInstance()
        imageView = view.findViewById(R.id.iv_sketch)

        val buttonImage = view.findViewById<Button>(R.id.btn_image_pick)
        buttonImage.setOnClickListener { arg0 ->
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, SELECT_IMAGE)
        }

        // 设置默认图片
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false)
        rgba.recycle()

        imageView?.setImageBitmap(bitmap)

        selectClassifier(NcnnClassifier::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.classification_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val classifierCls: Class<out Classifier>
        when (item.itemId) {

            R.id.navigation_ncnn ->
                classifierCls = NcnnClassifier::class.java
            R.id.navigation_mnn ->
                classifierCls = MnnClassifier::class.java
            R.id.navigation_pytorch ->
                classifierCls = PyTorchClassifier::class.java
            else ->
                throw IllegalStateException("Unsupport action ${item.itemId}")
        }
        selectClassifier(classifierCls)
        return true
    }


    private fun selectClassifier(classifierCls: Class<out Classifier>) {

        mClassifier?.release()

        mClassifier = classifierCls.constructors.first().newInstance(context) as Classifier
        debug("selectClassifier ${classifierCls.simpleName}")
        (activity as AppCompatActivity)?.supportActionBar?.title = classifierCls.simpleName
        mClassifier?.init(object : AoeClient.OnInitListener() {
            override fun onSuccess() {
                super.onSuccess()
                debug("AoeClient init success")
                if (yourSelectedImage != null) {
                    val result = mClassifier?.classifier(yourSelectedImage!!)
                    if (result is String) {
                        infoResult?.text = result
                    } else {
                        infoResult?.text = "detect failed"
                    }
                }
            }

            override fun onFailed(code: Int, msg: String) {
                super.onFailed(code, msg)
                debug("AoeClient init failed: $msg")
            }
        })

    }

    override fun onDestroy() {
        mClassifier?.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && null != data) {
            val selectedImage = data.data

            if (requestCode == SELECT_IMAGE) {
                try {
                    val bitmap = decodeUri(selectedImage)

                    val rgba = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)

                    // resize to 227x227
                    yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false)

                    rgba.recycle()

                    imageView!!.setImageBitmap(bitmap)
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "FileNotFoundException")
                    return
                }

            }

            if (yourSelectedImage == null)
                return

            val result = mClassifier?.classifier(yourSelectedImage!!)

            if (result is String) {
                infoResult?.text = result
            } else {
                infoResult?.text = "detect failed"
            }
        }
    }

    @Throws(FileNotFoundException::class)
    private fun decodeUri(selectedImage: Uri?): Bitmap? {
        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context!!.contentResolver.openInputStream(selectedImage!!), null, o)

        // The new size we want to scale to
        val REQUIRED_SIZE = 400

        // Find the correct scale value. It should be the power of 2.
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (width_tmp / 2 >= REQUIRED_SIZE && height_tmp / 2 >= REQUIRED_SIZE) {
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(context!!.contentResolver.openInputStream(selectedImage), null, o2)
    }

    companion object {
        private val SELECT_IMAGE = 1
    }
}
