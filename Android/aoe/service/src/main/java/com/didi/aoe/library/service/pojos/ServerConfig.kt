package com.didi.aoe.library.service.pojos

import com.google.gson.annotations.SerializedName

class ServerConfig {
    @SerializedName("appId")
    val appId: Long? = null

    @SerializedName("appKey")
    val appKey: String? = null

    @SerializedName("upgradeUrl")
    val upgradeUrl: String? = null

}