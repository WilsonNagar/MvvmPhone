package com.wilson.mvvmphone.module

import com.wilson.mvvmphone.MainActivity
import com.wilson.mvvmphone.auth.FragmentBindingModule
import com.wilson.mvvmphone.auth.MainActivityModule
import com.wilson.mvvmphone.scope.ActivityScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainActivityModule::class, FragmentBindingModule::class])
    internal abstract fun bindAuthActivity(): MainActivity

//    @ActivityScoped
//    @ContributesAndroidInjector(modules = [HomeActivityModule::class])
//    internal abstract fun bindGoalsActivity(): HomeActivity
}