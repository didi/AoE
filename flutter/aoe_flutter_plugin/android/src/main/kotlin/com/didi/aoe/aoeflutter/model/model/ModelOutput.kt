package com.didi.aoe.aoeflutter.model.model

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
data class ModelOutput @JvmOverloads constructor(
        var data: Any? = null,
        var error: InterceptorError? = null,
        var performance: PerformanceInfo? = null) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
                Pair("data", if (data != null) data else Any()),
                Pair("error", if (error != null) error?.toMap() else emptyMap()),
                Pair("performance", if (performance != null) performance?.toMap() else emptyMap())
        )
    }
}


