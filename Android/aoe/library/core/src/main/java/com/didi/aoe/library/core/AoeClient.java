package com.didi.aoe.library.core;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_UNDEFINE;

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

    @AoeProcessor.StatusCode
    private int mStatusCode = STATUS_UNDEFINE;

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

        tryLoadModelOptions(context, modelLoader, mainModelDir, subsequentModelDirs);

        mProcessor = new AoeProcessorImpl(context, options);
        mProcessor.setId(clientId);

    }

    /**
     * 初始化、加载模型文件
     *
     * @param listener
     */
    public void init(@Nullable AoeProcessor.OnInitListener listener) {

        initInternal(listener);

    }

    private void tryLoadModelOptions(@NonNull Context context,
                                     @NonNull AoeProcessor.ModelOptionLoaderComponent modelLoader,
                                     String mainModelDir,
                                     String... subsequentModelDirs) {
        AoeModelOption modelOption = modelLoader.load(context, mainModelDir);
        if (modelOption == null || !modelOption.isValid()) {
            mLogger.debug("Model init error: " + modelOption);
            mStatusCode = AoeProcessor.StatusCode.STATUS_CONFIG_PARSE_ERROR;
            return;
        }

        final List<AoeModelOption> options = new ArrayList<>();
        options.add(modelOption);

        if (subsequentModelDirs != null) {
            // 处理子模型
            for (String modelDir : subsequentModelDirs) {
                AoeModelOption subModelOption = modelLoader.load(context, modelDir);
                if (subModelOption != null && subModelOption.isValid()) {

                    options.add(subModelOption);

                } else {
                    mLogger.debug("Subsequent model init error: " + modelOption);
                    mStatusCode = AoeProcessor.StatusCode.STATUS_CONFIG_PARSE_ERROR;
                    return;
                }
            }
        }

        mModelOptions.clear();
        mModelOptions.addAll(options);
    }

    private void initInternal(@Nullable final AoeProcessor.OnInitListener listener) {
        AoeProcessor.InitResult initResult = AoeProcessor.InitResult.create(mStatusCode);

        if (isModelOptionReady()) {
            if (STATUS_UNDEFINE != mStatusCode) {
                if (listener != null) {
                    listener.onInitResult(initResult);
                }
                return;
            }
            mProcessor.getInterpreterComponent().init(mContext, mModelOptions, new AoeProcessor.OnInitListener() {
                @Override
                public void onInitResult(AoeProcessor.InitResult result) {
                    mStatusCode = result.getCode();

                    if (listener != null) {
                        listener.onInitResult(result);
                    }
                }
            });
        } else if (listener != null) {
            listener.onInitResult(initResult);
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
        return AoeProcessor.StatusCode.STATUS_OK == mStatusCode;
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

        @IntRange(from = 1)
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

        public Options setThreadNum(@IntRange(from = 1) int threadNum) {
            this.threadNum = threadNum;
            return this;
        }

    }
}

