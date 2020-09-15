package com.wilson.mvvmphone.repositories

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FireAuthRepository @Inject constructor(val firebaseAuth: FirebaseAuth) {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationId: String
    private lateinit var phoneCallbacksListener:PhoneCallbacksListener


    fun setPhoneCallbacksListener(listner: PhoneCallbacksListener) {
        this.phoneCallbacksListener = listner
    }

    init {
        firebaseAuth.setLanguageCode(Locale.getDefault().language)
    }

    private val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    phoneCallbacksListener.onVerificationCodeDetected(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                        phoneCallbacksListener.onVerificationFailed(e.message?:" ")
                    }
                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                        phoneCallbacksListener.onVerificationFailed(e.message?:" ")
                    }
                    else -> {
                        phoneCallbacksListener.onVerificationFailed(e.message?:" ")
                    }
                }
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
                resendToken = forceResendingToken
                phoneCallbacksListener.onCodeSent(s, forceResendingToken)
            }
        }

    fun sendVerificationCode(phone: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone.trim(),
            30,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            callbacks
        )
    }

//    fun verifyVerificationCode(code: String): PhoneAuthCredential {
//        return PhoneAuthProvider.getCredential(verificationId, code)
//    }

    fun verityVerificationCode(code:String) = flow<State<AuthResult>>{
        emit(State.loading())
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val verifyTask = firebaseAuth.signInWithCredential(credential).await()

        emit(State.success(verifyTask))

            /*.addOnCompleteListener(
                this
            ) {
                if (it.isSuccessful) {
                    showSnackBar("Verification Success")
                    firebaseAuth.signOut()
                } else {
                    // Show Error
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        showSnackBar(it.exception?.message?: "Verification Failed")
                    } else {
                        showSnackBar("Verification Failed")
                    }
                }
            }*/
    }.catch {
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun resendCode(phone: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 30, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, callbacks, resendToken)
    }

    fun isUserVerified(): Boolean {
        //Log.d("WOW TAG",if(firebaseAuth.currentUser != null)"User loged in " else "No user available")
        return firebaseAuth.currentUser != null
    }
}

interface PhoneCallbacksListener {
    fun onVerificationCompleted()
    fun onVerificationCodeDetected(code: String)
    fun onVerificationFailed(message: String)
    fun onCodeSent(
        verificationId: String?,
        token: PhoneAuthProvider.ForceResendingToken?
    )
}