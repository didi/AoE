package com.didi.aoe.library.api;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_CONFIG_PARSE_ERROR;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_CONNECTION_WAITING;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_INNER_ERROR;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_MODEL_DOWNLOAD_WAITING;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_MODEL_LOAD_FAILED;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_OK;
import static com.didi.aoe.library.api.AoeProcessor.StatusCode.STATUS_UNDEFINE;

/**
 * AoE 推理处理器
 *
 * @author noctis
 */
public interface AoeProcessor {
    /**
     * 跨进程通信需要配对服务委托者，每个Client应指定唯一ID
     *
     * @param id ClientId
     */
    void setId(String id);

    @NonNull
    InterpreterComponent getInterpreterComponent();

    @Nullable
    ParcelComponent getParcelComponent();

    interface Component {
    }

    /**
     * 模型配置加载组件
     */
    @FunctionalInterface
    interface ModelOptionLoaderComponent extends Component {
        AoeModelOption load(@NonNull Context ctx, @NonNull String modelDir);
    }

    /**
     * 模型翻译组件
     */
    interface InterpreterComponent<TInput, TOutput> extends Component {
        /**
         * 初始化，推理框架加载模型资源
         *
         * @param context      上下文，用与服务绑定
         * @param modelOptions 模型配置列表
         * @return 推理框架加载
         */
        void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable OnInitListener listener);

        /**
         * 执行推理操作
         *
         * @param input 业务输入数据
         * @return 业务输出数据
         */
        @Nullable
        TOutput run(@NonNull TInput input);

        /**
         * 释放资源
         */
        void release();

        /**
         * 模型是否正确加载完成
         *
         * @return true，模型正确加载
         */
        boolean isReady();

    }

    /**
     * 初始化监听
     */
    interface OnInitListener {
        void onInitResult(@NonNull InitResult result);
    }

    /**
     * 状态码，用于区分模型加载过程中的各种状态
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_UNDEFINE, STATUS_OK, STATUS_CONFIG_PARSE_ERROR, STATUS_CONNECTION_WAITING, STATUS_MODEL_DOWNLOAD_WAITING, STATUS_MODEL_LOAD_FAILED, STATUS_INNER_ERROR})
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
    @interface StatusCode {
        /**
         * 内部处理异常
         */
        int STATUS_INNER_ERROR = -2;
        /**
         * 初始状态
         */
        int STATUS_UNDEFINE = -1;

        int STATUS_OK = 0;
        /**
         * 模型配置加载失败
         */
        int STATUS_CONFIG_PARSE_ERROR = 1;
        /**
         * 使用独立进程，服务初次启动的过程是异步进行的，无法同步拿到初始化结果
         */
        int STATUS_CONNECTION_WAITING = 2;
        /**
         * 无内置模型，需要云端加载完成
         */
        int STATUS_MODEL_DOWNLOAD_WAITING = 3;
        /**
         * 模型加载失败
         */
        int STATUS_MODEL_LOAD_FAILED = 4;

    }

    /**
     * 初始化结果
     */
    class InitResult {
        @AoeProcessor.StatusCode
        private int code;

        private String msg;

        private InitResult(@AoeProcessor.StatusCode int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @AoeProcessor.StatusCode
        public int getCode() {
            return code;
        }

        public void setCode(@AoeProcessor.StatusCode int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "InitResult{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    '}';
        }

        public static InitResult create(@AoeProcessor.StatusCode int code) {
            return new InitResult(code, generalCodeName(code));
        }

        /**
         * 默认按CodeName填充msg字段
         *
         * @param code
         * @return
         */
        private static String generalCodeName(int code) {
            String codeName;
            switch (code) {
                case STATUS_INNER_ERROR:
                    codeName = "STATUS_INNER_ERROR";
                    break;
                case STATUS_UNDEFINE:
                    codeName = "STATUS_UNDEFINE";
                    break;
                case STATUS_OK:
                    codeName = "STATUS_OK";
                    break;
                case STATUS_CONFIG_PARSE_ERROR:
                    codeName = "STATUS_CONFIG_PARSE_ERROR";
                    break;
                case STATUS_CONNECTION_WAITING:
                    codeName = "STATUS_CONNECTION_WAITING";
                    break;
                case STATUS_MODEL_DOWNLOAD_WAITING:
                    codeName = "STATUS_MODEL_DOWNLOAD_WAITING";
                    break;
                case STATUS_MODEL_LOAD_FAILED:
                    codeName = "STATUS_MODEL_LOAD_FAILED";
                    break;
                default:
                    codeName = "UNKNOWN";
                    break;
            }
            return codeName;
        }

        public static InitResult create(@AoeProcessor.StatusCode int code, String msg) {
            return new InitResult(code, msg);
        }
    }

    /**
     * 数据预处理、后处理转换器接口
     */
    interface MultiConvertor<TInput, TOutput, TModelInput, TModelOutput> {
        /**
         * 数据预处理，将输入数据转换成模型输入数据
         *
         * @param input 业务输入数据
         * @return 模型输入数据
         */
        @Nullable
        TModelInput[] preProcessMulti(@NonNull TInput input);

        /**
         * 数据后处理，将模型输出数据转换成业务输出数据
         *
         * @param modelOutput 模型输出数据
         * @return 业务输出数据
         */
        @Nullable
        TOutput postProcessMulti(@Nullable Map<Integer, TModelOutput> modelOutput);
    }

    interface Convertor<TInput, TOutput, TModelInput, TModelOutput> {
        /**
         * 数据预处理，将输入数据转换成模型输入数据
         *
         * @param input 业务输入数据
         * @return 模型输入数据
         */
        @Nullable
        TModelInput preProcess(@NonNull TInput input);

        /**
         * 数据后处理，将模型输出数据转换成业务输出数据
         *
         * @param modelOutput 模型输出数据
         * @return 业务输出数据
         */
        @Nullable
        TOutput postProcess(@Nullable TModelOutput modelOutput);
    }

    /**
     * 序列化组件，用于跨进程通信的对象序列化与反序列化
     */
    interface ParcelComponent extends Component {
        /**
         * 对象序列化为字节数组
         *
         * @param obj 待序列化对象，依赖序列化方案
         * @return 序列化数据
         */
        byte[] obj2Byte(@NonNull Object obj);

        /**
         * 字节数组反序列化为对象实例
         *
         * @param bytes 反序列化需要的字节数组信息
         * @return 数据对象实例
         */
        Object byte2Obj(@NonNull byte[] bytes);
    }
}
