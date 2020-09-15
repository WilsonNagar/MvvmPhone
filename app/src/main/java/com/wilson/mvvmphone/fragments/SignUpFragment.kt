package com.wilson.mvvmphone.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentSignUpBinding
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.models.Users
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SignUpFragment : BaseFragment<FragmentSignUpBinding, MainActivityViewModel>() {
    companion object {
        fun newInstance(): SignUpFragment {
            val args = Bundle()
            val fragment = SignUpFragment()
            fragment.arguments = args
            return fragment
        }
    }
    override fun layoutId(): Int = R.layout.fragment_sign_up
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java
    private val uiScope = CoroutineScope(Dispatchers.Main)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnCreateData()
        setupOnClicks()
    }
    fun setupOnCreateData(){
        viewModel.currentFragment = 2
        viewModel.currentState = 1

        viewModel.selectedOtpNumber.value = ""
        with(viewModel){
            if(currentSubState==0){startLoginState()}
            else if(currentSubState==1){startSignupState()}

            selectedPhoneNumber.observe(
                requireActivity(),
                Observer<String?> { value ->
                    binding.signupAddPhoneText.setText(value ?: "")
                })
            selectedOtpNumber.observe(
                requireActivity(),
                Observer<String?> { value ->
                    binding.signupAddOtp.setText(value ?: "")
                })
            tempUsername1.observe(
                requireActivity(),
                Observer<String?> { value ->
                    binding.signupAddFnameText.setText(value ?: "")
                })
            tempUsername2.observe(
                requireActivity(),
                Observer<String?> { value ->
                    binding.signupAddLnameText.setText(value ?: "")
                })
            tempUserId.observe(
                requireActivity(),
                Observer<String?> { value ->
                    binding.signupAddIdText.setText(value ?: "")
                })
            if(tempGender==1){
                binding.signupGenderM.scaleY = 1.25f
                binding.signupGenderM.alpha = 1f
            }
            else if(tempGender==3){
                binding.signupGenderF.scaleY = 1.25f
                binding.signupGenderF.alpha = 1f
            }
        }
    }
    fun setupOnClicks(){
        binding.signupNext.setOnClickListener {
            if(binding.signupMotionlayout.progress.equals(0.0f)||binding.signupMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentState==1){
                    if(viewModel.currentSubState==1){
                        if(checkError1()) {
                            viewModel.tempUsername1.value = binding.signupAddFnameText.text.toString()
                            viewModel.tempUsername2.value = binding.signupAddLnameText.text.toString()
                            viewModel.tempUserId.value = binding.signupAddIdText.text.toString()
                            binding.signupNext.isEnabled=false
                            binding.signupBack.isEnabled=false
                            uiScope.launch {
                                binding.signupBackLogin.isEnabled=false
                                checkIfUserExists()
                            }
                           /* binding.signupText2.text = binding.signupAddFnameText.text.toString()
                            binding.signupMotionlayout.transitionToState(R.id.signup_scene2)
                            Handler().postDelayed({
                                binding.signupText2.setText(binding.signupAddFnameText.text)
                                viewModel.currentState = 2
                            }, 250)*/
                        }
                    }
                    else if(viewModel.currentSubState==0) {
                        if (checkError0()) {
                            viewModel.tempUserId.value = binding.signupAddIdText.text.toString()
                            binding.signupNext.isEnabled=false
                            binding.signupBack.isEnabled=false
                            uiScope.launch {
                              checkIfUserExists()
                            }
                            /*b1inding.signupMotionlayout.transitionToState(R.id.signup_scene2)
                            Handler().postDelayed({
                                //binding.signupText2.setText(binding.signupAddFnameText.text)
                                viewModel.currentState = 2
                            }, 250)*/
                        }
                    }
                }
                else if(viewModel.currentState==2 && checkErrors2()){
                    viewModel.selectedPhoneNumber.value = binding.signupAddPhoneText.text.toString()
                    binding.signupOtpMsg.text = "OTP was sent to \n+91"+viewModel.selectedPhoneNumber.value
                    viewModel.sendOtpToPhone("+91"+viewModel.selectedPhoneNumber.value.toString())
                    binding.signupMotionlayout.transitionToState(R.id.signup_scene3)
                    viewModel.currentState=3
                }
                else if(viewModel.currentState==3 && checkErrors3()){
                    viewModel.selectedOtpNumber.value = binding.signupAddOtp.text.toString()
                    //viewModel.verifyOtp(viewModel.selectedOtpNumber.value.toString())
                    binding.signupNext.isEnabled=false
                    binding.signupBack.isEnabled=false
                    uiScope.launch {
                        checkVerifyOTP()
                    }
                    //it.findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
                }
            }
        }
        binding.signupBack.setOnClickListener {
            if(binding.signupMotionlayout.progress.equals(0.0f)||binding.signupMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentState==2){
                    binding.signupMotionlayout.transitionToState(R.id.signup_scene1)
                    Handler().postDelayed({
                        binding.signupText2.setText("lets introduce")
                        viewModel.currentState=1
                    }, 250)
                }
                else if(viewModel.currentState==3){
                    binding.signupMotionlayout.transitionToState(R.id.signup_scene2)
                    viewModel.currentState=2
                }

            }
        }
        binding.signupRequestphone.setOnClickListener {
            viewModel.requestPhoneNumber()
        }
        binding.signupGenderM.setOnClickListener {
            if(viewModel.tempGender==3){
                binding.signupGenderF.alpha = 0.3f
                binding.signupGenderM.alpha = 1f
                viewModel.tempGender= 1
            }
            else if(viewModel.tempGender==2){
                binding.signupGenderM.alpha = 1f
                viewModel.tempGender= 1
            }
            else{
                binding.signupGenderM.alpha = 0.3f
                viewModel.tempGender = 2
            }

        }
        binding.signupGenderF.setOnClickListener {
            if(viewModel.tempGender==1){
                binding.signupGenderM.alpha = 0.3f
                binding.signupGenderF.alpha = 1f
                viewModel.tempGender = 3
            }
            else if(viewModel.tempGender==2){
                binding.signupGenderF.alpha = 1f
                viewModel.tempGender = 3
            }
            else{
                binding.signupGenderF.alpha = 0.3f
                viewModel.tempGender = 2
            }
        }
        binding.signupResendOTP.setOnClickListener {
            viewModel.resendOtp()
            //Toast.makeText(requireContext(),"No resend available",Toast.LENGTH_SHORT).show()
            binding.signupResendOTP.alpha=0f
            binding.signupResendOTP.isEnabled = false
        }
        binding.signupBackLogin.setOnClickListener {
            if(binding.signupMotionlayout.progress.equals(0.0f)||binding.signupMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentSubState==0){
                    viewModel.currentSubState=1
                    startSignupState()
                }
                else{
                    viewModel.currentSubState=0
                    startLoginState()
                }
            }
        }
        binding.signupText1.setOnClickListener{give_data()}
    }

    fun startSignupState(){
        binding.signupQ1.text = "You are..."
        binding.signupQ1.textSize = 24f
        binding.signupGenderM.visibility = View.VISIBLE
        binding.signupGenderF.visibility = View.VISIBLE
        binding.signupAddFname.visibility = View.VISIBLE
        binding.signupAddLname.visibility = View.VISIBLE
        binding.signupBackLogin.text = "Back to Login"
        binding.signupText2.text="lets introduce"

        binding.signupAddIdText.clearFocus()
        binding.signupAddId.error = ""
    }
    fun startLoginState(){
        viewModel.tempUsername1.value = ""
        viewModel.tempUsername2.value = ""
        binding.signupQ1.text = "May I have your ID please?\n( ID Podu )"
        binding.signupQ1.textSize = 42f
        binding.signupGenderM.visibility = View.GONE
        binding.signupGenderF.visibility = View.GONE
        binding.signupAddFname.visibility = View.GONE
        binding.signupAddLname.visibility = View.GONE
        binding.signupBackLogin.text = "Back to SignUp"
        binding.signupText2.text=""
        clearErrors1()
    }

    fun checkError0():Boolean{
        with(binding){
            signupAddId.error = ""
            signupAddIdText.clearFocus()
            if(signupAddIdText.text.toString().length!=15){
                signupAddId.error = "eg:RA1811000000000"
                signupAddIdText.requestFocus()
            }
            else if(!signupAddIdText.text.toString().substring(0,2).equals("RA")){
                signupAddId.error = "Invalid ID"
                signupAddIdText.requestFocus()
            }
            else if(!signupAddIdText.text.toString().substring(2,15).matches(Regex("^[0-9]*$"))){
                signupAddId.error = "Invalid ID Number"
                signupAddIdText.requestFocus()
            }
            else{
                return true
            }
        }
        return false
    }
    fun checkError1():Boolean{
        with(binding){
            clearErrors1()
            if(viewModel.tempGender==2){
                showToast("Choose Mr / Ms")
            }
            else if(signupAddFnameText.text.toString().length<3){
                signupAddFname.error = "Enter First Name Here"
                signupAddFnameText.requestFocus()
            }
            else if(!signupAddFnameText.text.toString().matches(Regex("^[a-zA-Z]*$"))){
                signupAddFname.error = "Contains Only Alphabets"
                signupAddFnameText.requestFocus()
            }
            else if(signupAddFnameText.text.toString().length>15){
                signupAddFname.error = "Name limit exceeded"
                signupAddFnameText.requestFocus()
            }
            else if(signupAddLnameText.text.toString().length<3){
                signupAddLname.error = "Enter Last Name Here"
                signupAddLnameText.requestFocus()
            }
            else if(!signupAddLnameText.text.toString().matches(Regex("^[a-zA-Z]*$"))){
                signupAddLname.error = "Contains Only Alphabets"
                signupAddLnameText.requestFocus()
            }
            else if(signupAddLnameText.text.toString().length>20){
                signupAddLname.error = "Name limit exceeded"
                signupAddLnameText.requestFocus()
            }
            else if(signupAddIdText.text.toString().length!=15){
                signupAddId.error = "eg:RA1811000000000"
                signupAddIdText.requestFocus()
            }
            else if(!signupAddIdText.text.toString().substring(0,2).equals("RA")){
                signupAddId.error = "Invalid ID"
                signupAddIdText.requestFocus()
            }
            else if(!signupAddIdText.text.toString().substring(2,15).matches(Regex("^[0-9]*$"))){
                signupAddId.error = "Invalid ID Number"
                signupAddIdText.requestFocus()
            }
            else{
                return true
            }
        }
        return false
    }
    fun checkErrors2():Boolean{
        with(binding){
            signupAddPhoneText.clearFocus()
            signupAddPhone.error=""
            if(signupAddPhoneText.text.toString().length<10){
                signupAddPhone.error="Phone no. contains 10 digits"
            }
            else if(signupAddPhoneText.text.toString().length>10){
                signupAddPhone.error="Number exceeded"
            }
            else if(!signupAddPhoneText.text.toString().matches(Regex("^[0-9]*$"))){
                signupAddPhone.error="Invalid Phone Number"
            }
            else{
                if(viewModel.currentSubState==0){
                    if(signupAddPhoneText.text.toString() == viewModel.tempUser.phno)return true
                    else{ signupAddPhone.error="Number does not match with ID"}
                }
                else{
                    return true
                }
            }
            signupAddPhoneText.requestFocus()
        }
        return false
    }
    fun checkErrors3():Boolean{
        with(binding){
            signupAddOtp.clearFocus()
            if(signupAddOtp.text.toString().length<6){
                signupOtpMsg.text="OTP contains 6 digits"
            }
            else if(!signupAddOtp.text.toString().matches(Regex("^[0-9]*$"))){
                signupOtpMsg.text="Invalid OTP"
            }
            else{
                return true
            }
            signupAddOtp.requestFocus()
        }
        return false
    }
    fun clearErrors1(){
        with(binding){
            signupAddIdText.clearFocus()
            signupAddFnameText.clearFocus()
            signupAddLnameText.clearFocus()
            signupAddFname.error = ""
            signupAddId.error = ""
            signupAddLname.error = ""
        }
    }

    private suspend fun checkIfUserExists() {
        viewModel.checkCustomUser().collect { state ->
            when (state) {
                is State.Loading -> {
                    showToast("Loading")
                }
                is State.Success -> {
                    viewModel.tempUser = state.data
                    viewModel.tempUser.phno = viewModel.tempUser.phno.toString().substring(viewModel.tempUser.phno.toString().length-10)
                    if(viewModel.currentSubState==1) {
                        binding.signupAddId.error = "ID Already Exists, Try Login"
                    }
                    else {
                        binding.signupMotionlayout.transitionToState(R.id.signup_scene2)
                        Handler().postDelayed({
                           binding.signupText2.setText(viewModel.tempUser.fname)
                           viewModel.currentState=2
                        }, 250)
                    }
                    binding.signupBackLogin.isEnabled=true
                    binding.signupNext.isEnabled=true
                    binding.signupBack.isEnabled=true
                }
                is State.Failed -> {
                    if(viewModel.currentSubState==0){
                        binding.signupAddId.error = "ID does not exists, Try Signing Up"
                    }
                    else{
                        binding.signupMotionlayout.transitionToState(R.id.signup_scene2)
                        Handler().postDelayed({
                            binding.signupText2.setText(binding.signupAddFnameText.text)
                            viewModel.currentState=2
                        }, 250)
                    }
                    binding.signupBackLogin.isEnabled=true
                    binding.signupNext.isEnabled=true
                    binding.signupBack.isEnabled=true
//                    showToast(state.message)
                }
            } // END when
        } // END collect
    }

    private suspend fun checkVerifyOTP(){
        viewModel.verifyOtp().collect{state ->
            when(state){
                is State.Loading ->{
                    binding.signupOtpMsg.text = "Loading..."
                }
                is State.Success -> {
                    showToast("Verification Success")
                    if(viewModel.currentSubState==0){
                        viewModel.addUsertoDatabase()
                        Handler().postDelayed({
                            binding.signupNext.findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
                        }, 250)
                    }
                    else {
                        with(viewModel){
                            if(tempUser.id.toString() != tempUserId.value.toString()){
                                tempUser = Users(tempUserId.value.toString(),tempUsername1.value.toString(),tempUsername2.value.toString(),selectedPhoneNumber.value.toString(),tempGender)
                            }
                        }
                        addNewUser()
                    }
                    //binding.signupOtpMsg.text = "Verification Success"

                }
                is State.Failed -> {
                    //showToast(state.message)
                    binding.signupOtpMsg.text = "Incorrect OTP"
                    binding.signupNext.isEnabled=true
                    binding.signupBack.isEnabled=true
                }
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun give_data(){
        Log.d("ViewData","state : "+viewModel.currentState+" , substate : "+viewModel.currentSubState)
    }

    private suspend fun addNewUser(){
        viewModel.addUsertoCloud().collect{state ->
            when(state){
                is State.Loading ->{
                    binding.signupOtpMsg.text = "Loading......"
                }
                is State.Success -> {
                    showToast("Registered Successfully")
                    viewModel.addUsertoDatabase()
                    binding.signupNext.findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
                }
                is State.Failed -> {
                    if(state.message=="postRef must not be null"){
                        showToast("Registered Successfully")
                        viewModel.addUsertoDatabase()
                        binding.signupNext.findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
                    }
                    else{
                        showToast(state.message)
                        viewModel.signOutUser()
                        binding.signupOtpMsg.text = ""
                        binding.signupNext.isEnabled=true
                        binding.signupBack.isEnabled=true
                    }
                }
            }
        }
    }
}