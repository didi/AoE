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
package com.didi.aoe.library.core

import android.content.Context
import android.support.annotation.IntRange
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor
import com.didi.aoe.library.api.AoeProcessor.InterpreterComponent
import com.didi.aoe.library.api.AoeProcessor.ModelOptionLoaderComponent
import com.didi.aoe.library.api.ParcelComponent
import com.didi.aoe.library.api.StatusCode
import com.didi.aoe.library.api.interpreter.InterpreterInitResult
import com.didi.aoe.library.api.interpreter.InterpreterInitResult.Companion.create
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener
import com.didi.aoe.library.common.stat.StatInfo
import com.didi.aoe.library.common.stat.TimeStat.Companion.getCostTime
import com.didi.aoe.library.common.stat.TimeStat.Companion.setEnd
import com.didi.aoe.library.common.stat.TimeStat.Companion.setStart
import com.didi.aoe.library.lang.AoeIOException
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.library.modeloption.loader.pojos.LocalModelOption
import java.io.Serializable
import java.util.*

/**
 * AoE业务交互终端。
 *
 * @author noctis
 */
class AoeClient {
    private val mLogger = LoggerFactory.getLogger("AoeClient")

    private val mContext: Context
    private var mClientId: String
    // AoE 执行器
    private val mProcessor: AoeProcessor
    // Client 配置信息
    private val mOptions: Options
    // 模型配置信息
    private val mModelOptions: MutableList<AoeModelOption> = ArrayList()
    // 翻译器初始化状态
    private var mStatusResult = create(StatusCode.STATUS_UNDEFINE)

    /**
     * 默认单模型构造方法
     *
     * @param context      全局上下文
     * @param options      Client配置，用来指定组件实例和运行模式
     * @param mainModelDir 模型配置目录
     */
    constructor(context: Context,
            options: Options,
            mainModelDir: String) : this(context, mainModelDir, options, mainModelDir)

    /**
     * 对于单一模型，提供简化调用方式
     *
     * @param context
     * @param interpreter
     * @param assetsModelPath assets模型文件全路径
     */
    constructor(context: Context,
            interpreter: InterpreterComponent<*, *>,
            assetsModelPath: String) {
        mContext = context
        mClientId = assetsModelPath
        val options: MutableList<AoeModelOption> = ArrayList()
        val index = assetsModelPath.lastIndexOf('/')
        val dir = assetsModelPath.substring(0, if (index < 0) 0 else index)
        val fileName = assetsModelPath.substring(if (index < 0) 0 else index + 1)
        options.add(LocalModelOption(dir, fileName))
        mModelOptions.clear()
        mModelOptions.addAll(options)
        mOptions = Options()
                .setInterpreter(interpreter)
                .useRemoteService(false)
        mProcessor = AoeProcessorImpl(context, mOptions)
        mProcessor.setId(mClientId)
    }

    /**
     * @param context             全局上下文
     * @param clientId            区分业务实现的ID（自定义）
     * @param options             Client配置，用来指定组件实例和运行模式
     * @param mainModelDir        主模型配置目录
     * @param subsequentModelDirs 子模型配置目录（用于多模型融合场景）
     */
    constructor(context: Context,
            clientId: String,
            options: Options,
            mainModelDir: String,
            vararg subsequentModelDirs: String) {
        mContext = context
        mClientId = clientId
        mOptions = options
        val modelLoader = ComponentProvider.getModelLoader(options.modelOptionLoaderClassName)
        try {
            tryLoadModelOptions(context, modelLoader, mainModelDir, *subsequentModelDirs)
        } catch (e: AoeIOException) {
            mStatusResult = create(StatusCode.STATUS_CONFIG_PARSE_ERROR, "ModelOption parse error: ${e.message}")
        }
        mProcessor = AoeProcessorImpl(context, options)
        mProcessor.setId(mClientId)
    }

    /**
     * 初始化、加载模型文件
     *
     * @param listener
     */
    fun init(listener: OnInitListener?) {
        mLogger.debug("[init]")
        initInternal(listener)
    }

    @Throws(AoeIOException::class)
    private fun tryLoadModelOptions(context: Context,
            modelLoader: ModelOptionLoaderComponent,
            mainModelDir: String,
            vararg subsequentModelDirs: String) {
        val modelOption = modelLoader.load(context, mainModelDir)
        mLogger.debug("[tryLoadModelOptions] ModelOption: $modelOption")
        if (modelOption == null) {
            throw AoeIOException("ModelOption load error, no main model.")
        }
        val options: MutableList<AoeModelOption> = ArrayList()
        options.add(modelOption)
        // 处理子模型
        for (modelDir in subsequentModelDirs) {
            val subModelOption = modelLoader.load(context, modelDir)
            mLogger.debug("Subsequent model: $subModelOption")
            if (subModelOption == null) {
                throw AoeIOException("ModelOption load error, no sub model.")
            }
            options.add(subModelOption)
        }
        mModelOptions.clear()
        mModelOptions.addAll(options)
    }

    private fun initInternal(listener: OnInitListener?) {
        if (StatusCode.STATUS_UNDEFINE != mStatusResult.code
                && StatusCode.STATUS_MODEL_DOWNLOAD_WAITING != mStatusResult.code) {
            // 已执行初始化，直接返回当前状态

            dispatchInitResult(mStatusResult, listener)
            return
        }
        if (isModelOptionReady) {
            val interpreterOptions = InterpreterComponent.Options()
            interpreterOptions.numThreads = mOptions.threadNum
            mProcessor.interpreterComponent
                    .init(mContext, interpreterOptions, mModelOptions, object : OnInterpreterInitListener {
                        override fun onInitResult(result: InterpreterInitResult) {
                            mStatusResult = result
                            mLogger.debug("onInitResult ${result.code}")
                            dispatchInitResult(result, listener)
                        }
                    })
        } else {
            dispatchInitResult(mStatusResult, listener)
        }
    }

    private fun dispatchInitResult(result: InterpreterInitResult,
            listener: OnInitListener?) {
        if (listener != null) {
            if (StatusCode.STATUS_OK == result.code) {
                listener.onSuccess()
            } else {
                listener.onFailed(result.code, result.msg)
            }
        }
    }

    /**
     * 模型加载完毕，待执行推理操作
     *
     * @return true, 模型加载完毕
     */
    val isRunning: Boolean
        get() = mProcessor.interpreterComponent.isReady

    /**
     * 模型配置读取正常
     *
     * @return true, 配置非空。
     */
    private val isModelOptionReady: Boolean
        private get() = mModelOptions.isNotEmpty()

    /**
     * 模型加载正常
     *
     * @return true，模型加载正常
     */
    private val isModelReady: Boolean
        private get() = StatusCode.STATUS_OK == mStatusResult.code

    fun process(input: Any?): Any? {
        if (!isModelReady) {
            initInternal(null)
            return null
        }
        setStart(generalClientKey("process"), System.currentTimeMillis())
        val result = mProcessor.interpreterComponent.run(input!!)
        setEnd(generalClientKey("process"), System.currentTimeMillis())
        return result
    }

    /**
     * 释放资源
     */
    fun release() {
        mLogger.debug("[release]")
        mProcessor.interpreterComponent.release()
    }

    /**
     * 获取最新的统计信息
     *
     * @return
     */
    fun acquireLatestStatInfo(): StatInfo {
        return StatInfo(
                getCostTime(generalClientKey("process"))
        )
    }

    private fun generalClientKey(key: String): String {
        return key + mClientId
    }

    /**
     * Client配置项
     */
    class Options : Serializable {
        var modelOptionLoaderClassName: String? = null
            private set
        var interpreterClassName: String? = null
            private set
        var parcelerClassName: String? = null
            private set
        /**
         * 使用独立进程进行模型加载和推理, 默认true
         */
        var isUseRemoteService = true
            private set
        /**
         * 当[.useRemoteService] = false 时，优先应用 interpreter 实例，当未指定时，使用interpreterClassName构造
         */
        var interpreter: InterpreterComponent<*, *>? = null
            private set
        @IntRange(from = 1, to = MAX_THREAD_NUM)
        var threadNum = 1
            private set

        /**
         * 设置模型配置加载器，适用与自定义模型配置策略
         *
         * @param modelOptionLoader
         * @return
         */
        fun setModelOptionLoader(
                modelOptionLoader: Class<out ModelOptionLoaderComponent?>): Options {
            modelOptionLoaderClassName = modelOptionLoader.name
            return this
        }

        fun setInterpreter(
                interpreter: Class<out InterpreterComponent<*, *>?>): Options {
            interpreterClassName = interpreter.name
            return this
        }

        fun setInterpreter(interpreter: InterpreterComponent<*, *>): Options {
            this.interpreter = interpreter
            return this
        }

        /**
         * 当 [.useRemoteService] 为 true 时，设置有效，用于处理对象的序列化与反序列化。
         *
         * @param parceler
         * @return
         */
        fun setParceler(parceler: Class<out ParcelComponent?>): Options {
            parcelerClassName = parceler.name
            return this
        }

        /**
         * 设置是否使用独立进程运行推理服务
         *
         * @param useRemoteService 默认true
         * @return
         */
        fun useRemoteService(useRemoteService: Boolean): Options {
            isUseRemoteService = useRemoteService
            return this
        }

        /**
         * 设置推理引擎执行线程数，如推理引擎不支持，则设置失效，默认为引擎推荐设置
         *
         * @param threadNum
         * @return
         */
        fun setThreadNum(@IntRange(from = 1, to = MAX_THREAD_NUM) threadNum: Int): Options {
            this.threadNum = threadNum
            return this
        }

        companion object {
            private const val MAX_THREAD_NUM: Long = 16
        }
    }

    /**
     * AoeClient 初始化监听
     */
    open class OnInitListener {
        open fun onSuccess() {}
        open fun onFailed(@StatusCode code: Int, msg: String?) {}
    }
}