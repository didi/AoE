package com.didi.aoe.features.squeeze.extension;

import android.content.Context;
import android.support.annotation.NonNull;

import com.didi.aoe.library.AoeRuntimeException;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.utils.FileUtil;
import com.didi.aoe.library.modeloption.loader.utils.JsonUtil;

import java.io.File;

/**
 * @author noctis
 */
public class SqueezeModelLoaderImpl implements AoeProcessor.ModelOptionLoaderComponent {

    private static final String CONFIG_FILE_NAME = "model.config";
    private final Logger mLogger = LoggerFactory.getLogger("ModelLoader");

    @Override
    public AoeModelOption load(@NonNull Context ctx, @NonNull String modelDir) {
        AoeModelOption option = null;
        String config = null;
        try {
            config = FileUtil.readString(ctx.getAssets().open(modelDir + File.separator + CONFIG_FILE_NAME));
        } catch (Exception e) {
            mLogger.error("readInternalConfig failed:", e);
        }
        if (config != null) {
            option = JsonUtil.objectFromJson(config, SqueezeModelOption.class);
        }
        mLogger.debug("readInternalConfig: " + modelDir);
        //make sure each filed is not empty
        if (option != null && !option.isValid()) {
            throw new AoeRuntimeException("Some field of this config is empty: " + option.toString());
        }
        return option;
    }
}
