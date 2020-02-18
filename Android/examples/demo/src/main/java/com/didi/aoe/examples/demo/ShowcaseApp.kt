package com.didi.aoe.examples.demo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.didi.aoe.library.service.AoeDataProvider
import com.didi.aoe.library.service.AoeService

/**
 * @author noctis
 */
class ShowcaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        AoeService.init(applicationContext, object : AoeDataProvider {
            override fun appId(): Long {
                return 10
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

}