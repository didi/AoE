package com.didi.aoe.features.squeeze;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.pojos.SqueezeModelOption;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author noctis
 */
public class SqueezeInterpreter implements AoeProcessor.InterpreterComponent<Bitmap, String> {
    private final Logger mLogger = LoggerFactory.getLogger("SqueezeInterpreter");
    private final SqueezeNcnn squeezencnn = new SqueezeNcnn();

    @Override
    public boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions) {
        if (modelOptions.size() != 1) {
            return false;
        }
        AoeModelOption option = modelOptions.get(0);
        if (option instanceof SqueezeModelOption) {
            SqueezeModelOption modelOption = (SqueezeModelOption) option;
            String paramFilePath = option.getModelDir() + "/" + modelOption.getModelParamFileName();
            String binFilePath = option.getModelDir() + "/" + modelOption.getModelFileName();
            String labelFilePath = option.getModelDir() + "/" + modelOption.getLabelFileName();
            try {
                initSqueezeNcnn(context, paramFilePath, binFilePath, labelFilePath);
                return true;
            } catch (IOException e) {
                mLogger.error("SqueezeInterpreter init error", e);
            }
        }

        return false;
    }

    @Override
    public String run(@NonNull Bitmap input) {
        return squeezencnn.detect(input);
    }

    @Override
    public void release() {

    }

    @Override
    public boolean isReady() {
        return false;
    }

    private void initSqueezeNcnn(Context context, String paramFilePath, String binFilePath, String labelFilePath) throws IOException {
        byte[] param;
        byte[] bin;
        byte[] words;

        {
            InputStream assetsInputStream = context.getAssets().open(paramFilePath);
            int available = assetsInputStream.available();
            param = new byte[available];
            int byteCode = assetsInputStream.read(param);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = context.getAssets().open(binFilePath);
            int available = assetsInputStream.available();
            bin = new byte[available];
            int byteCode = assetsInputStream.read(bin);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = context.getAssets().open(labelFilePath);
            int available = assetsInputStream.available();
            words = new byte[available];
            int byteCode = assetsInputStream.read(words);
            assetsInputStream.close();
        }

        squeezencnn.init(param, bin, words);
    }
}
