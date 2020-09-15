package com.wilson.mvvmphone.base

import com.wilson.mvvmphone.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import com.wilson.mvvmphone.component.DaggerAppComponent

class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            //Timber.plant(Timber.DebugTree())
        }
    }
}