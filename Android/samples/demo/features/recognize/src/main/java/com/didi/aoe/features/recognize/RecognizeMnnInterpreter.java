package com.didi.aoe.features.recognize;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.runtime.mnn.MNNImageProcess;
import com.didi.aoe.runtime.mnn.MNNInterpreter;
import com.didi.aoe.runtime.mnn.MNNNetInstance;
import com.taobao.android.utils.Common;
import com.taobao.android.utils.TxtFileReader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RecognizeMnnInterpreter extends MNNInterpreter<Bitmap, String> {
    private final Logger mLogger = LoggerFactory.getLogger("MnistMnnInterpreter");
    private final String MobileWordsFileName = "recognize-mnn/synset_words.txt";

    private List<String> mMobileTaiWords;
    private final int InputWidth = 224;
    private final int InputHeight = 224;

    @Nullable
    @Override
    public MNNNetInstance.Session.Tensor preProcess(@NonNull Bitmap input) {
        mLogger.debug("===MnistMnnInterpreter：" + input);
        /*
         *  convert data to input tensor
         */
        final MNNImageProcess.Config config = new MNNImageProcess.Config();
        // normalization params
        config.mean = new float[]{103.94f, 116.78f, 123.68f};
        config.normal = new float[]{0.017f, 0.017f, 0.017f};
        // input data format
        config.dest = MNNImageProcess.Format.BGR;
        // bitmap transform
        Matrix matrix = new Matrix();
        matrix.postScale(InputWidth / (float) input.getWidth(), InputHeight / (float) input.getHeight());
        matrix.invert(matrix);

        MNNImageProcess.convertBitmap(input, mInputTensor, config, matrix);

        return mInputTensor;
    }

    @Nullable
    @Override
    public String postProcess(@Nullable MNNNetInstance.Session.Tensor outputTensor) {
        if (mMobileTaiWords == null) {
            prepareMobileNet();
        }
        float[] result = outputTensor.getFloatData();// get float results
        // 显示结果
        List<Map.Entry<Integer, Float>> maybes = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            float confidence = result[i];
            if (confidence > 0.01) {
                maybes.add(new AbstractMap.SimpleEntry<Integer, Float>(i, confidence));
            }
        }
        Log.i(Common.TAG, "Inference result size=" + result.length + ", maybe=" + maybes.size());

        Collections.sort(maybes, new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                if (Math.abs(o1.getValue() - o2.getValue()) <= Float.MIN_NORMAL) {
                    return 0;
                }
                return o1.getValue() > o2.getValue() ? -1 : 1;
            }
        });

        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Float> entry : maybes) {
            sb.append("物体:");
            sb.append(mMobileTaiWords.get(entry.getKey()));
            sb.append(" 置信度:");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void prepareMobileNet() {
        try {
            mMobileTaiWords = TxtFileReader.getUniqueUrls(mAppContext, MobileWordsFileName, Integer.MAX_VALUE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
