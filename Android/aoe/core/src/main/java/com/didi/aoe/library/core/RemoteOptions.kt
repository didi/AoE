package com.didi.aoe.library.core

import com.didi.aoe.library.api.AoeModelOption
import java.io.Serializable

/**
 * 包装Client和Model的配置类，用于跨进程传递给RemoteService初始化
 * [com.didi.aoe.library.api.AoeProcessor]
 *
 * @author noctis
 */
internal class RemoteOptions : Serializable {
    var clientOptions: AoeClient.Options? = null
    var modelOptions: List<AoeModelOption>? = null

}