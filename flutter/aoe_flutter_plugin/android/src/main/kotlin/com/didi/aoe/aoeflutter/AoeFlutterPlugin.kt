package com.didi.aoe.aoeflutter

import android.content.Context
import com.didi.aoe.aoeflutter.model.model.InterceptorError
import com.didi.aoe.aoeflutter.model.model.ModelOutput
import com.didi.aoe.aoeflutter.model.model.PerformanceInfo
import com.didi.aoe.library.core.AoeClient
import com.didi.aoe.library.logging.LoggerFactory
import com.example.aoe_flutter.interpreter.MnistTensorFlowLiteInterpreter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** AoeFlutterPlugin */
class AoeFlutterPlugin(private val mContext: Context) : FlutterPlugin, MethodCallHandler {
    private val mLogger = LoggerFactory.getLogger("AoeFlutterPlugin")
    private var mAoeClient: AoeClient? = null

    private val mPerf = PerformanceInfo()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(flutterPluginBinding.binaryMessenger, "aoeflutter")
        channel.setMethodCallHandler(AoeFlutterPlugin(flutterPluginBinding.applicationContext));
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "aoeflutter")
            channel.setMethodCallHandler(AoeFlutterPlugin(registrar.context()))
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if ("start" == call.method) {
            start(call, result)
        } else if ("process" == call.method) {
            process(call, result)
        } else if ("stop" == call.method) {
            stop(call, result)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {

    }

    private fun start(call: MethodCall, result: MethodChannel.Result) {
        val inputs = call.arguments as Map<*, *>
        val runtime = inputs["runtime"]
        val modelDir = inputs["modelDir"] as String
        mLogger.debug("runtime: $runtime, modelDir: $modelDir")
        if (inputs.isEmpty()) {
            result.error("", InterceptorError.parameterError.errMsg, InterceptorError.parameterError.toMap())
            return
        }
        // Fixme 临时处理
        val aoeClient = AoeClient(mContext,
                AoeClient.Options()
                        .setInterpreter(MnistTensorFlowLiteInterpreter::class.java),
                modelDir)
        mAoeClient = aoeClient
        mAoeClient?.init(object : AoeClient.OnInitListener() {
            override fun onSuccess() {
                super.onSuccess()
                mLogger.debug("init Success")
                result.success(ModelOutput(data = true).toMap())
            }

            override fun onFailed(code: Int, msg: String?) {
                super.onFailed(code, msg)
                mLogger.debug("init Failed $code $msg")
                result.error(code.toString(), msg,
                        ModelOutput(error = InterceptorError.modelError).toMap())
            }
        })

    }

    private fun process(call: MethodCall, result: MethodChannel.Result) {

        val input = call.argument<Any>("data")
        val output = mAoeClient?.process(input)
        val statInfo = mAoeClient?.acquireLatestStatInfo()

        if (statInfo != null) {
            mPerf.time.tracks(statInfo.timeCostInMills.toFloat())
            mPerf.cpu.tracks(statInfo.cpuRate)
            mPerf.mem.tracks(statInfo.memoryInfo)
        }
        val out = ModelOutput(data = output, performance = mPerf)
        mLogger.debug("process $out")
        result.success(out.toMap())
    }

    private fun stop(call: MethodCall, result: MethodChannel.Result) {
        mAoeClient?.release()
        result.success(ModelOutput(data = true).toMap())
    }
}
