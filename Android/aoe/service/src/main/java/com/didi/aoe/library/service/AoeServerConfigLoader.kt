package com.didi.aoe.library.service

import android.content.Context
import com.didi.aoe.library.common.util.FileUtils
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.library.service.pojos.ServerConfig
import com.google.gson.Gson
import java.io.File

class AoeServerConfigLoader {
    private val mLogger = LoggerFactory.getLogger("AoeServerConfigLoader")
    fun load(context: Context, serverConfigDir: String): ServerConfig? {
        return readServerConfig(context, serverConfigDir)
    }

    private fun readServerConfig(context: Context, serverConfigDir: String): ServerConfig? {
        mLogger.debug("readServerConfig: $serverConfigDir")
        var config: String? = null
        try {
            config = FileUtils.readString(context.assets.open(serverConfigDir + File.separator + AoeServerConfigLoader.CONFIG_FILE_NAME))
        } catch (e: Exception) {
            mLogger.error("readServerConfig failed:", e)
        }
        return parseModelOption(config)
    }

    companion object {
        private const val CONFIG_FILE_NAME = "server.config"
    }

    private fun parseModelOption(config: String?): ServerConfig? {
        var option: ServerConfig? = null
        if (config != null) {
            option = objectFromJson(config, ServerConfig::class.java)
        }
        //make sure each filed is not empty
        if (option != null) {
            mLogger.warn("Some field of this config is empty: $option")
        }
        return option
    }

    private fun <T> objectFromJson(json: String?, klass: Class<T>?): T? {
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
}