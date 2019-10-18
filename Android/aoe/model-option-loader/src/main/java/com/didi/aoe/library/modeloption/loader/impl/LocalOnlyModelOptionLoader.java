package com.didi.aoe.library.modeloption.loader.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.didi.aoe.library.AoeRuntimeException;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.pojos.LocalModelOption;
import com.didi.aoe.library.modeloption.loader.utils.FileUtil;
import com.didi.aoe.library.modeloption.loader.utils.JsonUtil;

import java.io.File;

/**
 * AoE 模型配置的缺省本地加载器
 *
 * @author noctis
 */
public class LocalOnlyModelOptionLoader implements AoeProcessor.ModelOptionLoaderComponent {

    private static final String CONFIG_FILE_NAME = "model.config";
    private final Logger mLogger = LoggerFactory.getLogger("LocalOnlyModelOptionLoader");

    @Override
    public AoeModelOption load(@NonNull Context ctx, @NonNull String modelDir) {
        LocalModelOption option = null;
        String config = null;
        try {
            config = FileUtil.readString(ctx.getAssets().open(modelDir + File.separator + CONFIG_FILE_NAME));
        } catch (Exception e) {
            mLogger.error("Read LocalModelOption failed: ", e);
        }
        if (config != null) {
            option = JsonUtil.objectFromJson(config, LocalModelOption.class);
            mLogger.debug("Parse LocalModelOption: " + option);
        }

        //make sure each filed is not empty
        if (option != null && !option.isValid()) {
            throw new AoeRuntimeException("Some field of this config is empty: " + option.toString());
        }

        return option;
    }
}
