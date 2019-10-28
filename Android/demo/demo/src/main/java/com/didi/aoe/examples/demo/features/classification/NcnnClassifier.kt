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

import android.content.Context
import android.graphics.Bitmap
import com.didi.aoe.extensions.parcel.kryo.KryoParcelImpl
import com.didi.aoe.features.squeeze.SqueezeInterpreter
import com.didi.aoe.features.squeeze.extension.SqueezeModelLoaderImpl
import com.didi.aoe.library.core.AoeClient

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
class NcnnClassifier constructor(context: Context) : Classifier {

    private val mClient: AoeClient = AoeClient(context, "squeeze",
            AoeClient.Options()
                    .setModelOptionLoader(SqueezeModelLoaderImpl::class.java)
                    .setInterpreter(SqueezeInterpreter::class.java)
                    .setParceler(KryoParcelImpl::class.java)
                    .useRemoteService(false),
            "squeeze");

    override fun init(listener: AoeClient.OnInitListener) {
        mClient.init(listener)
    }

    override fun classifier(bitmap: Bitmap): Any? {
        return mClient.process(bitmap)
    }

    override fun release() {
        mClient.release()
    }
}