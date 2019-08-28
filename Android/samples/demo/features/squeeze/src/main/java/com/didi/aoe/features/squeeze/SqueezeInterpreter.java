package com.didi.aoe.features.squeeze;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.pojos.SqueezeModelOption;
import com.didi.aoe.runtime.ncnn.Interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noctis
 * @author fire9953
 */
public class SqueezeInterpreter implements AoeProcessor.InterpreterComponent<Bitmap, String> {
    private final Logger mLogger = LoggerFactory.getLogger("SqueezeInterpreter");

    private Interpreter squeeze;

    private static float meanVals[] = {104.f, 117.f, 123.f};
    private static float norVals[] = null;
    private static final int INPUT_WIDTH = 227;
    private static final int INPUT_HEIGHT = 227;
    private static final int INPUT_BLOB_INDEX = 0;
    private static final int OUTPUT_BLOB_INDEX = 82;

    private List<String> lableList = new ArrayList();

    @Override
    public boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions) {
        if (modelOptions.size() != 1) {
            return false;
        }

        // 这是后处理用的Label
        readLableFileOnce(context, "squeeze/synset_words.txt");

        // 加载模型
        AoeModelOption option = modelOptions.get(0);
        if (option instanceof SqueezeModelOption) {
            SqueezeModelOption modelOption = (SqueezeModelOption) option;

            squeeze = new Interpreter(null);
            squeeze.loadModelAndParam(context.getAssets(), option.getModelDir(),
                    modelOption.getModelFileName(), modelOption.getModelParamFileName(),
                    1, 1, INPUT_BLOB_INDEX, OUTPUT_BLOB_INDEX);

            return squeeze.isLoadModelSuccess();
        }

        return false;
    }

    @Override
    public String run(@NonNull Bitmap input) {
        int size = input.getByteCount();
        ByteBuffer bmpBuffer = ByteBuffer.allocate(size);
        input.copyPixelsToBuffer(bmpBuffer);
        byte[] rgba = bmpBuffer.array();

        squeeze.inputRgba(rgba, input.getWidth(), input.getHeight(), INPUT_WIDTH, INPUT_HEIGHT,
                meanVals, norVals, 0);

        ByteBuffer buffer = ByteBuffer.allocate(4096);
        squeeze.run(null, buffer);
        buffer.order(ByteOrder.nativeOrder());
        buffer.flip();

        // 后处理，根据自己的情况，可以用JAVA，也可以用C
        int[] shape = squeeze.getOutputTensor(0).shape();
        int top_class = 0;
        float max_score = 0.f;
        for (int i = 0; i < shape[0]; i++) {
            float s = buffer.getFloat();
            if (s > max_score) {
                top_class = i;
                max_score = s;
            }
        }

        return String.format("%s = %.3f", lableList.get(top_class).substring(10), max_score);
    }

    @Override
    public void release() {
        if (null != squeeze) {
            squeeze.close();
            squeeze = null;
        }
    }

    @Override
    public boolean isReady() {
        return false;
    }

    private void readLableFileOnce(Context context, String fileName) {
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            bufReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufReader.readLine()) != null) {
                lableList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputReader) {
                    inputReader.close();
                }

                if (null != bufReader) {
                    bufReader.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
