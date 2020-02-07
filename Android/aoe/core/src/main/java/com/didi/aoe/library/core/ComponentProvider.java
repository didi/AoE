package com.didi.aoe.library.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.ParcelComponent;
import com.didi.aoe.library.core.io.AoeParcelImpl;
import com.didi.aoe.library.lang.AoeRuntimeException;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.modeloption.loader.impl.LocalOnlyModelOptionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 组件发现器，通过manifest注册组件自动注册，提供Native/Remote进程组件实例
 *
 * @author noctis
 */
final class ComponentProvider {
    private static final Logger mLogger = LoggerFactory.getLogger("ComponentProvider");

    private static final Map<String, AoeProcessor.ModelOptionLoaderComponent> modelLoaderComponentMap = new HashMap<>();
    private static final Map<String, AoeProcessor.InterpreterComponent> interpreterComponentMap = new HashMap<>();
    private static final Map<String, ParcelComponent> parcelComponentMap = new HashMap<>();

    private ComponentProvider() {
    }

    @SuppressWarnings("unchecked")
    synchronized private static <T> void cacheServiceIfNeeded(String clzName,
                                                              @NonNull Class<T> clz,
                                                              @NonNull Map<String, T> cacheServices) {
        if (clzName == null || cacheServices.containsKey(clzName)) {
            // 当前实例已缓存或未指定，直接返回。
            return;
        }
        try {
            Class serviceCls = Class.forName(clzName);
            Object service = serviceCls.newInstance();
            if (!clz.isAssignableFrom(service.getClass())) {
                throw new AoeRuntimeException(service.getClass().getName() + " you registered is not an instance of " + clz.getName());
            }
            cacheServices.put(clzName, (T) service);
        } catch (Exception e) {
            mLogger.error("release error", e);
        }

    }


    @NonNull
    public static AoeProcessor.ModelOptionLoaderComponent getModelLoader(String clzName) {
        cacheServiceIfNeeded(clzName, AoeProcessor.ModelOptionLoaderComponent.class, modelLoaderComponentMap);

        AoeProcessor.ModelOptionLoaderComponent modelLoaderComponent = modelLoaderComponentMap.get(clzName);
        if (modelLoaderComponent == null) {
            // 未指定实现则使用AoE默认实现
            modelLoaderComponent = new LocalOnlyModelOptionLoader();
        }
        return modelLoaderComponent;
    }

    @Nullable
    public static AoeProcessor.InterpreterComponent getInterpreter(String clzName) {
        cacheServiceIfNeeded(clzName, AoeProcessor.InterpreterComponent.class, interpreterComponentMap);

        return interpreterComponentMap.get(clzName);
    }

    @NonNull
    public static ParcelComponent getParceler(String clzName) {
        cacheServiceIfNeeded(clzName, ParcelComponent.class, parcelComponentMap);

        ParcelComponent parcelComponent = parcelComponentMap.get(clzName);
        if (parcelComponent == null) {
            // 未指定实现则使用AoE默认实现
            parcelComponent = new AoeParcelImpl();
        }
        return parcelComponent;
    }
}
