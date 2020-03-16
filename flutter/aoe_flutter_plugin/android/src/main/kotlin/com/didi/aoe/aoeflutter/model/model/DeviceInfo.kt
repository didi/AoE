package com.didi.aoe.aoeflutter.model.model

/**
 *
 *
 * @author noctis
 * @since 1.1.0
 */
data class DeviceInfo @JvmOverloads constructor(
        var uuid: String = "",
        var name: String = "",
        var model: String = "",
        var system: String = "",
        var version: String = "",
        var disk: String = "",
        var memory: String = "",
        var ip: String = "",
        var macAddress: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
                Pair("uuid", uuid),
                Pair("name", name),
                Pair("model", model),
                Pair("system", system),
                Pair("version", version),
                Pair("disk", disk),
                Pair("memory", memory),
                Pair("ip", ip),
                Pair("macAddress", macAddress)
        )
    }
}