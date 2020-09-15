package com.wilson.mvvmphone.component

import android.app.Application
import com.wilson.mvvmphone.base.BaseApplication
import com.wilson.mvvmphone.module.*
import com.wilson.mvvmphone.scope.AppScoped
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@AppScoped
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityBindingModule::class,
    AppModule::class,
    ViewModelFactoryModule::class,
    NetworkModule::class,
    PersistanceModule::class
])
interface AppComponent : AndroidInjector<BaseApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}