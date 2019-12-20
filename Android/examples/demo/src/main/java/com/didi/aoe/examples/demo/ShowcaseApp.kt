package com.didi.aoe.examples.demo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.didi.aoe.library.service.AoeService
import com.didi.aoe.library.service.AoeDataProvider

/**
 * @author noctis
 */
class ShowcaseApp : Application(), CameraXConfig.Provider {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        AoeService.init(applicationContext, object : AoeDataProvider {
            override fun appId(): Long {
                return 164
            }

            override fun latitude(): Double {
                return 39.92
            }

            override fun longitude(): Double {
                return 116.46
            }
        })

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}