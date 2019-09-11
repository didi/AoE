package com.didi.aoe.library.core;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static com.didi.aoe.library.core.AoeClient.StatusCode.CONFIG_PARSE_ERROR;
import static com.didi.aoe.library.core.AoeClient.StatusCode.MODEL_LOAD_ERROR;
import static com.didi.aoe.library.core.AoeClient.StatusCode.MODEL_LOAD_OK;
import static com.didi.aoe.library.core.AoeClient.StatusCode.MODEL_LOAD_READY;
import static com.didi.aoe.library.core.AoeClient.StatusCode.UNDEFINE;

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

    @StatusCode
    private int mStatusCode;

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

        mStatusCode = tryLoadModelOptions(context, modelLoader, mainModelDir, subsequentModelDirs);

        mProcessor = new AoeProcessorImpl(context, options);
        mProcessor.setId(clientId);

    }

    /**
     * 初始化、加载模型文件
     *
     * @return **独立进程模式时，初始化操作委托给RemoteService完成，模型加载直接返回{@link StatusCode#MODEL_LOAD_OK}**
     */
    @StatusCode
    public int init() {

        return initInternal();

    }

    @StatusCode
    private int tryLoadModelOptions(@NonNull Context context,
                                    @NonNull AoeProcessor.ModelOptionLoaderComponent modelLoader,
                                    String mainModelDir,
                                    String... subsequentModelDirs) {
        AoeModelOption modelOption = modelLoader.load(context, mainModelDir);
        if (modelOption == null || !modelOption.isValid()) {
            mLogger.debug("Model init error: " + modelOption);
            return CONFIG_PARSE_ERROR;
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
                    return CONFIG_PARSE_ERROR;
                }
            }
        }

        mModelOptions.clear();
        mModelOptions.addAll(options);

        return MODEL_LOAD_READY;
    }

    @StatusCode
    private int initInternal() {
        if (isModelOptionReady()) {

            boolean initOk = mProcessor.getInterpreterComponent().init(mContext, mModelOptions);
            mLogger.debug("initInternal: " + initOk);
            if (!initOk) {
                mStatusCode = MODEL_LOAD_ERROR;
            } else {
                mStatusCode = MODEL_LOAD_OK;
            }
        }

        return mStatusCode;
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
     * @return true, 配置读取正常，有非空配置。
     */
    private boolean isModelOptionReady() {
        return MODEL_LOAD_READY <= mStatusCode
                && !mModelOptions.isEmpty();
    }

    /**
     * 模型加载正常
     *
     * @return true，模型加载正常
     */
    private boolean isModelReady() {
        return MODEL_LOAD_OK <= mStatusCode;
    }

    @WorkerThread
    @Nullable
    public Object process(Object input) {
        if (!isModelReady()) {
            initInternal();
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
     * Client状态定义
     */
    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({UNDEFINE, CONFIG_PARSE_ERROR, MODEL_LOAD_READY, MODEL_LOAD_ERROR, MODEL_LOAD_OK})
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
    public @interface StatusCode {
        int UNDEFINE = 0;
        /**
         * 模型配置读取错误
         */
        int CONFIG_PARSE_ERROR = 1;
        /**
         * 模型配置加载正常，准备加载模型
         */
        int MODEL_LOAD_READY = 2;
        /**
         * 模型文件加载错误
         */
        int MODEL_LOAD_ERROR = 3;

        /**
         * 模型加载完成
         */
        int MODEL_LOAD_OK = 4;


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

    }
}

