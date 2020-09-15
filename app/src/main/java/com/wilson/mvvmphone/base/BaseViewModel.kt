package com.wilson.mvvmphone.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<VI: ViewInteractor>: ViewModel() {

    //private val mCompositeDisposable: CompositeDisposable? = null

    var viewInteractor: VI? = null

    override fun onCleared() {
        //mCompositeDisposable?.dispose()
        super.onCleared()
    }
}