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
package com.didi.aoe.library.service

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextUtils
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor.ModelOptionLoaderComponent
import com.didi.aoe.library.common.util.FileUtils.getFilesDir
import com.didi.aoe.library.common.util.FileUtils.readString
import com.didi.aoe.library.lang.AoeIOException
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.library.service.ModelContract.ModelRequestCallback
import com.didi.aoe.library.service.pojos.ModelOption
import com.didi.aoe.library.service.pojos.UpgradeModelResult
import com.google.gson.Gson
import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * 模型动态升级服务配套模型加载组件
 *
 * @author noctis
 * @since 1.1.0
 */
class AoeModelOptionLoader() : ModelOptionLoaderComponent {
    private val mLogger = LoggerFactory.getLogger("ModelLoader")
    private val mHandlerThread: HandlerThread
    private val mModelRequestHandler: Handler
    @Throws(AoeIOException::class)
    override fun load(context: Context, modelDir: String): AoeModelOption {
        val internalOption = readInternalConfig(context, modelDir)
        val downloadOption = readDownloadConfig(context, getTopVersionModelDir(context, modelDir))
        val suggestedModelOption = calculateSuggestedModelOption(internalOption, downloadOption)
        upgradeIfNeeded(context, suggestedModelOption)
        if (suggestedModelOption == null) {
            throw AoeIOException("ModelOption not found.")
        }
        return suggestedModelOption
    }

    /**
     * 升级到最新状态
     *
     * @param context
     * @param option
     */
    private fun upgradeIfNeeded(context: Context, option: ModelOption?) {
        if (option == null) {
            mLogger.warn("upgradeIfNeeded: ModelOption required.")
            return
        }

        if (Looper.myLooper() == null) {
            mLogger.debug("[requestModel] inside work thread")
            return
        }

        // 请求新的版本模型
        val modelRequest = ModelRequest(AoeService.getInstance().dataProvider.appId(), option)
        ModelContract.requestModel(context, modelRequest, mModelRequestHandler, object : ModelRequestCallback() {
            override fun onModelRetrieved(model: UpgradeModelResult) {
                mLogger.debug("requestModel success: $model")
            }

            override fun onModelRequestFailed(code: Int, msg: String) {
                mLogger.info("requestModel failed: $code, msg: $msg")
            }
        })
    }

    /**
     * 比较选出合适的模型配置
     *
     * @param internalOption
     * @param downloadOption
     * @return
     */
    private fun calculateSuggestedModelOption(internalOption: ModelOption?,
            downloadOption: ModelOption?): ModelOption? {
        if (downloadOption == null || !downloadOption.isValid) { // 没有外置策略时使用内部策略
            return internalOption
        }
        if (internalOption == null || !internalOption.isValid) { // 只有外部策略时，使用外部策略，正常不应该执行到这个逻辑，内置配置默认需要配置。
            mLogger.warn("Internal option not found")
            return downloadOption
        }
        // 比较内外模型版本，选择高版本配置
        return if ((!TextUtils.isEmpty(internalOption.version)
                        && !TextUtils.isEmpty(downloadOption.version)
                        && (compareVersion((internalOption.version)!!, (downloadOption.version)!!) < 0))) {
            downloadOption
        } else {
            internalOption
        }
    }

    /**
     * 读取内置模型配置
     *
     * @param context
     * @param modelDir
     * @return
     */
    private fun readInternalConfig(context: Context, modelDir: String): ModelOption? {
        mLogger.debug("readInternalConfig: $modelDir")
        var config: String? = null
        try {
            config = readString(context.assets.open(modelDir + File.separator + CONFIG_FILE_NAME))
        } catch (e: Exception) {
            mLogger.error("readInternalConfig failed:", e)
        }
        return parseModelOption(config)
    }

    /**
     * 读取下载模型配置
     *
     * @param context
     * @param modelDir
     * @return
     */
    private fun readDownloadConfig(context: Context, modelDir: String?): ModelOption? {
        mLogger.debug("readDownloadConfig: $modelDir")
        if (TextUtils.isEmpty(modelDir)) {
            return null
        }
        val rootDir = File(getFilesDir(context) + File.separator + modelDir)
        if (rootDir.exists()) {
            val lastestConfigFile = rootDir.walk()
                    .maxDepth(2)
                    .filter { it.isFile }
                    .filter { it.extension in listOf("config") }
                    .sortedBy { it.lastModified() }
                    .firstOrNull()

            if (lastestConfigFile != null && lastestConfigFile.exists()) {
                val config = readString(lastestConfigFile.absolutePath)
                return parseModelOption(config)
            }
        }

        return null
    }

    private fun parseModelOption(config: String?): ModelOption? {
        var option: ModelOption? = null
        if (config != null) {
            option = objectFromJson(config, ModelOption::class.java)
        }
        //make sure each filed is not empty
        if (option != null && !option.isValid) {
            mLogger.warn("Some field of this config is empty: $option")
        }
        return option
    }

    fun <T> objectFromJson(json: String?, klass: Class<T>?): T? {
        if (json == null) {
            return null
        }
        try {
            return Gson().fromJson(json, klass)
        } catch (e: Exception) {
            mLogger.error("JsonUtil", e)
        }
        return null
    }

    /**
     * 下载模型文件按版本号解压到对应目录下，按最新版本号读取模型文件
     *
     * @param context
     * @param modelDir
     * @return
     */
    private fun getTopVersionModelDir(context: Context, modelDir: String): String? {
        val rootPath = getFilesDir(context)
        val dir = File(rootPath)
        if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles(
                    FileFilter { pathname -> pathname.isDirectory && pathname.name.startsWith(modelDir) })
            if (files != null && files.size > 0) {
                val topVersionFile =
                        Collections.max(Arrays.asList(*files),
                                object : Comparator<File> {
                                    override fun compare(o1: File, o2: File): Int {
                                        return compareVersion(o1.name, o2.name)
                                    }
                                })
                return topVersionFile.name
            }
        }
        return null
    }

    /**
     * 版本名比较，版本名应遵循 [{x}.].{x}
     *
     * @param v1
     * @param v2
     * @return
     */
    private fun compareVersion(v1: String, v2: String): Int {
        try {
            val versionArray1 = v1.split("\\.").toTypedArray() //注意此处为正则匹配，不能用"."；
            val versionArray2 = v2.split("\\.").toTypedArray()
            var idx = 0
            val minLength = Math.min(versionArray1.size, versionArray2.size) //取最小长度值
            var diff = 0
            while ((idx < minLength
                            ) && ((versionArray1[idx].length - versionArray2[idx].length.also {
                        diff = it
                    }) == 0 //先比较长度
                            ) && ((versionArray1[idx].compareTo(versionArray2[idx]).also { diff = it }) == 0)) { //再比较字符
                ++idx
            }
            //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
            diff = if ((diff != 0)) diff else versionArray1.size - versionArray2.size
            return diff
        } catch (e: Exception) {
            mLogger.error("compareVersion failed:", e)
        }
        return 0
    }

    companion object {
        private val CONFIG_FILE_NAME = "model.config"
    }

    init {
        mHandlerThread = HandlerThread("model-loader-worker")
        mHandlerThread.start()
        mModelRequestHandler = Handler(mHandlerThread.looper)
    }
}