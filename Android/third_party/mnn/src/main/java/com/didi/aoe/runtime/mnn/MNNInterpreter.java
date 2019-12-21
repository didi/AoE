package com.didi.aoe.runtime.mnn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.StatusCode;
import com.didi.aoe.library.api.convertor.Convertor;
import com.didi.aoe.library.api.interpreter.InterpreterInitResult;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.api.interpreter.SingleInterpreterComponent;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.*;

/**
 * 基于MNN的运行时Interpreter封装。
 *
 * @param <TInput>  范型，业务输入数据
 * @param <TOutput> 范型，业务输出数据
 * @author coleman
 */
public abstract class MNNInterpreter<TInput, TOutput> extends SingleInterpreterComponent<TInput, TOutput> implements
        Convertor<TInput, TOutput, MNNNetInstance.Session.Tensor, MNNNetInstance.Session.Tensor> {

    private final Logger mLogger = LoggerFactory.getLogger("MNNInterpreter");
    private MNNNetInstance mNetInstance;
    private MNNNetInstance.Session mSession;
    protected MNNNetInstance.Session.Tensor mInputTensor;
    protected Context mAppContext;

    @Override
    public void init(@NonNull Context context,
            @Nullable AoeProcessor.InterpreterComponent.Options interpreterOptions,
            @NonNull AoeModelOption modelOptions,
            @Nullable OnInterpreterInitListener listener) {
        this.mAppContext = context.getApplicationContext();
        String modelFilePath = modelOptions.getModelDir() + File.separator + modelOptions.getModelName() + ".mnn";
        try {
            // create net instance
            byte[] datas = read(context.getAssets().open(modelFilePath));
            mNetInstance = MNNNetInstance.createFromBuffer(datas, datas.length);
            // create session with config
            MNNNetInstance.Config config = new MNNNetInstance.Config();
            config.numThread = 4;// set threads
            config.forwardType = MNNForwardType.FORWARD_CPU.type;// set CPU/GPU
            mSession = mNetInstance.createSession(config);
            mInputTensor = mSession.getInput(null);
            if (listener != null) {
                listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_OK));
            }
            return;
        } catch (Exception e) {
            mLogger.error(e.getMessage());
        }

        if (listener != null) {
            listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_LOAD_FAILED));
        }
    }

    @Override
    @Nullable
    public TOutput run(@NonNull TInput input) {
        if (isReady()) {
            mInputTensor = preProcess(input);

            if (mInputTensor != null) {

                mSession.run();

                return postProcess(mSession.getOutput(null));
            }

        }
        return null;
    }

    @Override
    public void release() {
        mSession.release();
        mSession = null;
    }

    @Override
    public boolean isReady() {
        return mSession != null;
    }

    private byte[] read(InputStream is) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            bis = new BufferedInputStream(is);
            baos = new ByteArrayOutputStream();

            int len;

            byte[] buf = new byte[1024];

            while ((len = bis.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            return baos.toByteArray();

        } catch (Exception e) {
            mLogger.error("read InputStream error: ", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new byte[0];
    }
}
