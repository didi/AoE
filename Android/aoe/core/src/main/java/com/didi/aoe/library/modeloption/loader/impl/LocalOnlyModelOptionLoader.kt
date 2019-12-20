package com.didi.aoe.library.modeloption.loader.impl

import android.content.Context
import com.didi.aoe.library.api.AoeModelOption
import com.didi.aoe.library.api.AoeProcessor.ModelOptionLoaderComponent
import com.didi.aoe.library.common.util.FileUtils.read
import com.didi.aoe.library.lang.AoeIOException
import com.didi.aoe.library.logging.LoggerFactory
import com.didi.aoe.library.modeloption.loader.pojos.LocalModelOption
import com.google.gson.Gson
import java.io.File

/**
 * AoE 模型配置的缺省本地加载器
 *
 * @author noctis
 */
class LocalOnlyModelOptionLoader : ModelOptionLoaderComponent {
    private val mLogger =
            LoggerFactory.getLogger("LocalOnlyModelOptionLoader")

    @Throws(AoeIOException::class)
    override fun load(context: Context,
            modelDir: String): AoeModelOption {
        var option: LocalModelOption? = null
        var config: String? = null
        try {
            context.assets.open(modelDir + File.separator + CONFIG_FILE_NAME)
                    .use { input ->
                        config = String(read(input)!!)
                        option = Gson().fromJson(config, LocalModelOption::class.java)
                    }
        } catch (e: Exception) {
            throw AoeIOException("Parse LocalModelOption failed: ", e)
        }
        mLogger.debug("Parse LocalModelOption: $option")

        // make sure each field is not empty


        if (option == null || !option!!.isValid) {
            throw AoeIOException("Some fields of this config is empty: $option")
        }
        return option!!
    }

    companion object {
        private const val CONFIG_FILE_NAME = "model.config"
    }
}