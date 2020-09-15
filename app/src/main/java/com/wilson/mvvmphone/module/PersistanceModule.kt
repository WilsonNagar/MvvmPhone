package com.wilson.mvvmphone.module

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.wilson.mvvmphone.scope.AppScoped
import dagger.Module
import dagger.Provides

@Module
class PersistanceModule {

    @Provides
    @AppScoped
    internal fun provideSharedPreference(context: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}