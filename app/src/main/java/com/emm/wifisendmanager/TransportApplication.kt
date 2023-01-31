package com.emm.wifisendmanager

import android.app.Application

/**
 * @author dengjie
 * date 2023.01.28
 * description
 */
class TransportApplication : Application() {

    companion object{
        lateinit var appContext : TransportApplication
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}