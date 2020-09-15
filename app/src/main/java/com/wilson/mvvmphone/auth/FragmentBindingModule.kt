package com.wilson.mvvmphone.auth

import com.wilson.mvvmphone.fragments.*
import com.wilson.mvvmphone.scope.FragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindOnboardingFragment(): OnboardingFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindSignUpFragment(): SignUpFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindHomeFragment(): HomeFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindAddCabFragment(): AddCabFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindCabpoolFragment(): CabpoolFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindAddStoreFragment(): AddStoreFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindCategoryFragment(): CategoryFragment

}