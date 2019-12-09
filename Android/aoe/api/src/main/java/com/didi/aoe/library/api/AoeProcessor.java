package com.didi.aoe.library.api;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.lang.AoeIOException;

import java.util.List;

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
        AoeModelOption load(@NonNull Context ctx, @NonNull String modelDir) throws AoeIOException;
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
        void init(@NonNull Context context, @NonNull List<AoeModelOption> modelOptions, @Nullable OnInterpreterInitListener listener);

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
