package com.didi.aoe.aoeflutter.model.model

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
enum class InterceptorErrorType {
    InValidParameter, //参数异常
    InValidInterceptorError, //推理框架无效
    InValidModelError, //模型无效
    InValidSampleError, //测试数据集无效
    ProcessError, //推理异常
}

data class InterceptorError @JvmOverloads constructor(
        var errType: InterceptorErrorType = InterceptorErrorType.InValidParameter,
        var errMsg: String = ""

) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
                Pair("errType", errType),
                Pair("errMsg", errMsg)
        )
    }

    companion object {
        @JvmStatic
        val parameterError =
                InterceptorError(InterceptorErrorType.InValidParameter, "the parameters is invalid")

        @JvmStatic
        val interceptorError = InterceptorError(InterceptorErrorType.InValidInterceptorError,
                "can not suppport the infer inframework or can not load the model")

        @JvmStatic
        val modelError = InterceptorError(InterceptorErrorType.InValidModelError,
                "model does not exist")

        @JvmStatic
        val sampleError = InterceptorError(InterceptorErrorType.InValidSampleError,
                "sample data invalid")
    }
}