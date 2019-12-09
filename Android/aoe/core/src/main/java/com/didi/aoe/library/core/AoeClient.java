package com.didi.aoe.library.core;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.StatusCode;
import com.didi.aoe.library.api.interpreter.InterpreterInitResult;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.lang.AoeIOException;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.didi.aoe.library.api.StatusCode.*;

/**
 * AoE业务交互终端。
 *
 * @author noctis
 */
public final class AoeClient {

    private final Logger mLogger = LoggerFactory.getLogger("AoeClient");

    private final Context mContext;

    private final AoeProcessor mProcessor;

    private final List<AoeModelOption> mModelOptions = new ArrayList<>();

    private InterpreterInitResult mStatusResult = InterpreterInitResult.create(STATUS_UNDEFINE);

    /**
     * 默认单模型构造方法
     *
     * @param context      全局上下文
     * @param options      Client配置，用来指定组件实例和运行模式
     * @param mainModelDir 模型配置目录
     */
    public AoeClient(@NonNull Context context,
                     @NonNull Options options,
                     @NonNull String mainModelDir) {
        this(context, mainModelDir, options, mainModelDir);
    }

    /**
     * @param context             全局上下文
     * @param clientId            区分业务实现的ID（自定义）
     * @param options             Client配置，用来指定组件实例和运行模式
     * @param mainModelDir        主模型配置目录
     * @param subsequentModelDirs 子模型配置目录（用于多模型融合场景）
     */
    public AoeClient(@NonNull Context context,
                     @NonNull String clientId,
                     @NonNull Options options,
                     @NonNull String mainModelDir,
                     @Nullable String... subsequentModelDirs) {
        mContext = context;

        AoeProcessor.ModelOptionLoaderComponent modelLoader = ComponentProvider.getModelLoader(options.modelOptionLoaderClassName);

        try {
            tryLoadModelOptions(context, modelLoader, mainModelDir, subsequentModelDirs);
        } catch (AoeIOException e) {
            mStatusResult = InterpreterInitResult.create(StatusCode.STATUS_CONFIG_PARSE_ERROR, "ModelOption parse error: " + e.getMessage());
        }

        mProcessor = new AoeProcessorImpl(context, options);
        mProcessor.setId(clientId);

    }

    /**
     * 初始化、加载模型文件
     *
     * @param listener
     */
    public void init(@Nullable OnInitListener listener) {

        initInternal(listener);

    }

    private void tryLoadModelOptions(@NonNull Context context,
                                     @NonNull AoeProcessor.ModelOptionLoaderComponent modelLoader,
                                     String mainModelDir,
                                     String... subsequentModelDirs) throws AoeIOException {
        AoeModelOption modelOption = modelLoader.load(context, mainModelDir);
        mLogger.debug("[tryLoadModelOptions] ModelOption: " + modelOption);

        if (modelOption == null) {
            throw new AoeIOException("ModelOption load error, no main model.");
        }

        final List<AoeModelOption> options = new ArrayList<>();
        options.add(modelOption);

        if (subsequentModelDirs != null) {
            // 处理子模型
            for (String modelDir : subsequentModelDirs) {
                AoeModelOption subModelOption = modelLoader.load(context, modelDir);
                mLogger.debug("Subsequent model: " + subModelOption);
                if (subModelOption == null) {
                    throw new AoeIOException("ModelOption load error, no sub model.");
                }
                options.add(subModelOption);
            }
        }

        mModelOptions.clear();
        mModelOptions.addAll(options);
    }

    private void initInternal(@Nullable final OnInitListener listener) {

        if (STATUS_UNDEFINE != mStatusResult.getCode()
                && STATUS_MODEL_DOWNLOAD_WAITING != mStatusResult.getCode()) {
            // 已执行初始化，直接返回当前状态
            dispatchInitResult(mStatusResult, listener);
            return;
        }

        if (isModelOptionReady()) {

            mProcessor.getInterpreterComponent().init(mContext, mModelOptions, new OnInterpreterInitListener() {
                @Override
                public void onInitResult(@NonNull InterpreterInitResult result) {
                    mStatusResult = result;

                    dispatchInitResult(result, listener);
                }

            });
        } else {
            dispatchInitResult(mStatusResult, listener);
        }
    }

    private void dispatchInitResult(@NonNull InterpreterInitResult result, @Nullable OnInitListener listener) {
        if (listener != null) {
            if (STATUS_OK == result.getCode()) {
                listener.onSuccess();
            } else {
                listener.onFailed(result.getCode(), result.getMsg());
            }
        }
    }

    /**
     * 模型加载完毕，待执行推理操作
     *
     * @return true, 模型加载完毕
     */
    public boolean isRunning() {
        return mProcessor.getInterpreterComponent().isReady();
    }

    /**
     * 模型配置读取正常
     *
     * @return true, 配置非空。
     */
    private boolean isModelOptionReady() {
        return !mModelOptions.isEmpty();
    }

    /**
     * 模型加载正常
     *
     * @return true，模型加载正常
     */
    private boolean isModelReady() {
        return STATUS_OK == mStatusResult.getCode();
    }

    @Nullable
    public Object process(Object input) {
        if (!isModelReady()) {
            initInternal(null);
            return null;
        }

        //noinspection unchecked
        return mProcessor.getInterpreterComponent().run(input);
    }

    /**
     * 释放资源
     */
    public void release() {
        mProcessor.getInterpreterComponent().release();
    }

    /**
     * Client配置项
     */
    public static class Options implements Serializable {
        String modelOptionLoaderClassName;
        String interpreterClassName;
        String parcelerClassName;
        /**
         * 使用独立进程进行模型加载和推理, 默认true
         */
        boolean useRemoteService = true;

        @IntRange(from = 1, to = 16)
        int threadNum = 1;

        public Options setModelOptionLoader(@NonNull Class<? extends AoeProcessor.ModelOptionLoaderComponent> modelOptionLoader) {
            this.modelOptionLoaderClassName = modelOptionLoader.getName();
            return this;
        }

        public Options setInterpreter(@NonNull Class<? extends AoeProcessor.InterpreterComponent> interpreter) {
            this.interpreterClassName = interpreter.getName();
            return this;
        }

        public Options setParceler(@NonNull Class<? extends AoeProcessor.ParcelComponent> parceler) {
            this.parcelerClassName = parceler.getName();
            return this;
        }

        public Options useRemoteService(boolean useRemoteService) {
            this.useRemoteService = useRemoteService;
            return this;
        }

        public Options setThreadNum(@IntRange(from = 1, to = 16) int threadNum) {
            this.threadNum = threadNum;
            return this;
        }

    }

    /**
     * AoeClient 初始化监听
     */
    public static class OnInitListener {
        public void onSuccess() {

        }

        public void onFailed(@StatusCode int code, String msg) {

        }
    }
}

