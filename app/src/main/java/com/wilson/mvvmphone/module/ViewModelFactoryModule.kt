package com.wilson.mvvmphone.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wilson.mvvmphone.scope.AppScoped
import com.wilson.mvvmphone.utils.ViewModelFactory
import com.wilson.mvvmphone.utils.ViewModelKey
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelFactoryModule {

    @Binds
    @AppScoped
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindAuthActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

//    @Binds
//    @IntoMap
//    @ViewModelKey(HomeActivityViewModel::class)
//    abstract fun bindGoalActivityViewModel(goalActivityViewModel: HomeActivityViewModel): ViewModel
}
