package com.didi.aoe.library.api;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

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
        boolean init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions);

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
