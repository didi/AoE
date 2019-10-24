package com.didi.aoe.library.modeloption.loader.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.common.util.FileUtils;
import com.didi.aoe.library.lang.AoeIOException;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.pojos.LocalModelOption;
import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;

/**
 * AoE 模型配置的缺省本地加载器
 *
 * @author noctis
 */
public final class LocalOnlyModelOptionLoader implements AoeProcessor.ModelOptionLoaderComponent {

    private static final String CONFIG_FILE_NAME = "model.config";
    private final Logger mLogger = LoggerFactory.getLogger("LocalOnlyModelOptionLoader");

    @Override
    public AoeModelOption load(@NonNull Context context, @NonNull String modelDir) throws AoeIOException {
        LocalModelOption option = null;
        String config = null;
        try (InputStream is = context.getAssets().open(modelDir + File.separator + CONFIG_FILE_NAME)) {
            config = new String(FileUtils.read(is));
            option = new Gson().fromJson(config, LocalModelOption.class);
        } catch (Exception e) {
            throw new AoeIOException("Parse LocalModelOption failed: ", e);
        }

        mLogger.debug("Parse LocalModelOption: " + option);

        // make sure each field is not empty
        if (option == null || !option.isValid()) {
            throw new AoeIOException("Some fields of this config is empty: " + option);
        }

        return option;
    }
}
