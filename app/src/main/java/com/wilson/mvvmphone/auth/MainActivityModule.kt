package com.wilson.mvvmphone.auth

import android.app.Application
import android.content.Context
import com.wilson.mvvmphone.MainActivity
import com.wilson.mvvmphone.repositories.BaseResourceProvider
import com.wilson.mvvmphone.repositories.ResourceProvider
import com.wilson.mvvmphone.scope.ActivityScoped
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerAppCompatActivity


@Module
class MainActivityModule {
    @Provides
    @ActivityScoped
    fun provideActivityContext(activity: MainActivity): Context {
        return activity
    }

    @Provides
    @ActivityScoped
    fun provideActivity(homeActivity: MainActivity): DaggerAppCompatActivity {
        return homeActivity
    }

    @Provides
    @ActivityScoped
    fun provideResourceProvider(context: Application): BaseResourceProvider {
        return ResourceProvider(context)
    }
//    @Provides
//    @ActivityScoped
//    fun provideAuthPagerAdapter(activity: AuthActivity): AuthPagerAdapter {
//        return AuthPagerAdapter(activity)
//    }
}