package com.didi.aoe.aoeflutter.model.model

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
data class PerformanceInfo @JvmOverloads constructor(
        var cpu: PerformanceItem = PerformanceItem(),
        var mem: PerformanceItem = PerformanceItem(),
        var time: PerformanceItem = PerformanceItem(),
        var device: DeviceInfo = DeviceInfo()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
                Pair("cpu", cpu.toMap()),
                Pair("mem", mem.toMap()),
                Pair("time", time.toMap()),
                Pair("device", device.toMap())

        )
    }
}

data class PerformanceItem @JvmOverloads constructor(
        var max: Float = 0.0f,
        var min: Float = 0.0f,
        var avg: Float = 0.0f,
        private var count: Int = 0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
                Pair("max", max),
                Pair("min", min),
                Pair("avg", avg)
        )
    }

    fun tracks(value: Float) {
        // 防止越界
        if (count > 10000) {
            count = 100
        }
        val sum = count * avg
        count += 1

        avg = (sum + value) / count

        if (max < value) {
            max = value
        }

        if (min > value) {
            min = value
        }
    }
}