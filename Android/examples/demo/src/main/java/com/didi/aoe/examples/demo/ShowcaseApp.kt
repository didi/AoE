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

        AoeService.init(applicationContext)

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }

}