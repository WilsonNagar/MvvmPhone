package com.wilson.mvvmphone.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wilson.mvvmphone.scope.AppScoped
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    @AppScoped
    internal fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @AppScoped
    internal fun provideFirebaseStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}