package com.didi.aoe.library.core;

import com.didi.aoe.library.api.AoeModelOption;

import java.io.Serializable;
import java.util.List;

/**
 * 包装Client和Model的配置类，用于跨进程传递给RemoteService初始化
 * {@link com.didi.aoe.library.api.AoeProcessor}
 *
 * @author noctis
 */
final class RemoteOptions implements Serializable {
    private AoeClient.Options clientOptions;
    private List<AoeModelOption> modelOptions;

    public AoeClient.Options getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(AoeClient.Options clientOptions) {
        this.clientOptions = clientOptions;
    }

    public List<AoeModelOption> getModelOptions() {
        return modelOptions;
    }

    public void setModelOptions(List<AoeModelOption> modelOptions) {
        this.modelOptions = modelOptions;
    }
}
