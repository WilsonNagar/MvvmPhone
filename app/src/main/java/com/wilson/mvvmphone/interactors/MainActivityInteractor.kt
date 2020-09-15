package com.wilson.mvvmphone.interactors

import com.google.firebase.auth.PhoneAuthCredential
import com.wilson.mvvmphone.base.ViewInteractor

interface MainActivityInteractor : ViewInteractor {
    fun showSnackBarMessage(message: String)
    fun startSMSListener()
    fun goToHomeFragment()
    fun requirePhone()
    fun setCalenderTime()
    fun setCalenderDate()
    fun selectImage()
    fun callPerson()
}