package com.wilson.mvvmphone.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentAddCabBinding
import com.wilson.mvvmphone.databinding.FragmentOnboardingBinding
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddCabFragment : BaseFragment<FragmentAddCabBinding, MainActivityViewModel>() {

    companion object {
        fun newInstance(): AddCabFragment {
            val args = Bundle()
            val fragment = AddCabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun layoutId(): Int = R.layout.fragment_add_cab
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java
    private val uiScope = CoroutineScope(Dispatchers.Main)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStartData()
        setupClickListeners()
    }
    private fun setupClickListeners() {
        binding.addCabNext.setOnClickListener {
            if (!isAnimating()) {
                if (viewModel.currentState == 1) {
                    if(checkError1(binding.addCabFromText.text.toString(),binding.addCabToText.text.toString())){
                        viewModel.fromPlace = binding.addCabFromText.text.toString()
                        viewModel.toPlace = binding.addCabToText.text.toString()
                        binding.addCabMotionlayout.transitionToState(R.id.add_cab_set2)
                        viewModel.currentState++
                    }
                } else if (viewModel.currentState == 2) {
                    if(checkError2(binding.addCabTime.text.toString(),binding.addCabDate.text.toString())){
//                        viewModel.timePlace = binding.addCabTime.text.toString()
//                        viewModel.datePlace = binding.addCabDate.text.toString()
                        binding.addCabMotionlayout.transitionToState(R.id.add_cab_set3)
                        viewModel.currentState++
                    }
                } else if (viewModel.currentState == 3) {
                    if(checkError3()){
                        binding.addCabMotionlayout.transitionToState(R.id.add_cab_set4)
                        viewModel.currentState++
                        binding.addCabNext.setText(R.string.confirm)
                    }
                } else if(viewModel.currentState == 4) {
                    if(checkError4()){
                        binding.addCabNext.isEnabled = false
                        binding.addCabBack.isEnabled = false
                        uiScope.launch {
                            addCab1()
                        }
                    }
                }
            }
        }
        binding.addCabBack.setOnClickListener {
            if (!isAnimating()) {
                if (viewModel.currentState == 2) {
                    binding.addCabMotionlayout.transitionToState(R.id.add_cab_set1)
                    viewModel.currentState--
                } else if (viewModel.currentState == 3) {
                    binding.addCabMotionlayout.transitionToState(R.id.add_cab_set2)
                    viewModel.currentState--
                } else if (viewModel.currentState == 4) {
                    binding.addCabNext.setText(R.string.next)
                    binding.addCabMotionlayout.transitionToState(R.id.add_cab_set3)
                    viewModel.currentState--
                }
            }
        }
        binding.addCabTime.setOnClickListener {
            if(!isAnimating()){
                viewModel.showCalenderTime()
            }
        }
        binding.addCabDate.setOnClickListener {
            if(!isAnimating()){
                viewModel.showCalenderDate()
            }
        }
        binding.addCabOnlygender.setOnClickListener {
            when(viewModel.peopleType){
                0 ->  {
                    binding.addCabOnlygender.alpha = 1f
                    viewModel.peopleType = 1
                }
                1 ->  {
                    binding.addCabOnlygender.alpha = 0.3f
                    viewModel.peopleType = 0
                }
                2 -> {
                    binding.addCabOnlygender.alpha = 1f
                    binding.addCabOpenall.alpha = 0.3f
                    viewModel.peopleType = 1
                }
                else ->{viewModel.peopleType = 0}
            }
            Log.d("wow","only clicked "+viewModel.peopleType)
        }
        binding.addCabOpenall.setOnClickListener {
            when(viewModel.peopleType){
                0 ->  {
                    binding.addCabOpenall.alpha = 1f
                    viewModel.peopleType = 2
                }
                1 ->  {
                    binding.addCabOnlygender.alpha = 0.3f
                    binding.addCabOpenall.alpha = 1f
                    viewModel.peopleType = 2
                }
                2 -> {
                    binding.addCabOpenall.alpha = 0.3f
                    viewModel.peopleType = 0
                }
                else ->{viewModel.peopleType = 0}
            }
            Log.d("wow","open clicked "+viewModel.peopleType)
        }
    }
    private fun setupStartData(){
        viewModel.currentFragment=4
        viewModel.currentState=1

        viewModel.chosenTime.observe(requireActivity(),
            Observer<String?> { value ->
                binding.addCabTime.text = value ?: "Set Time"
            })
        viewModel.chosenDate.observe(requireActivity(),
            Observer<String?> { value ->
                binding.addCabDate.text = value ?: "Set Date"
            })
        with(binding){
            addCabFromText.setText(viewModel.fromPlace)
            addCabToText.setText(viewModel.toPlace)
            if(viewModel.tempUser.gender==1){
                addCabOnlygenderText.text = "Only for\nBoys"
                addCabOnlygender.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.men_group, null))
                addCabOpenall.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_group, null))
            }
            else{
                addCabOnlygenderText.text = "Only for\nGirls"
                addCabOnlygender.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.women_group, null))
                addCabOpenall.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.gb_group, null))
            }
            when(viewModel.peopleType){
                1 -> {
                    binding.addCabOnlygender.alpha = 1f
                    binding.addCabOpenall.alpha = 0.3f
                }
                2 -> {
                    binding.addCabOnlygender.alpha = 0.3f
                    binding.addCabOpenall.alpha = 1f
                }
                else -> {
                    binding.addCabOnlygender.alpha = 0.3f
                    binding.addCabOpenall.alpha = 0.3f
                }
            }
        }
    }
    private fun checkError1(s1:String,s2:String):Boolean{
        with(binding){
            addCabFrom.error = ""
            addCabTo.error = ""
            addCabToText.clearFocus()
            addCabFromText.clearFocus()
            if(s1.length<3){
                addCabFrom.error = "Enter starting point"
                addCabFromText.requestFocus()
            }
            else if(s1.length>20){
                addCabFrom.error = "Text limit exceeded"
                addCabFromText.requestFocus()
            }
            else if(s2.length<2){
                addCabTo.error = "Enter destination point"
                addCabToText.requestFocus()
            }
            else if(s2.length>20){
                addCabTo.error = "Text limit exceeded"
                addCabToText.requestFocus()
            }
            else{
                return true
            }
        }
        return false
    }
    private fun checkError2(s1:String,s2:String):Boolean{
        if(s1 == "Set Time"){ showSnackBar("Set the Time of Departure") }
        else if(s2 == "Set Date"){ showSnackBar("Set the Date of Departure") }
        else{ return true }
        return false
    }
    private fun checkError3():Boolean{
        if(viewModel.peopleType==0)showSnackBar("Select a Option")
        else return true
        return false
    }
    private fun checkError4():Boolean{
        return true
    }
    private fun isAnimating():Boolean{
        return !(binding.addCabMotionlayout.progress.equals(0.0f) || binding.addCabMotionlayout.progress.equals(1.0f))
    }

    private suspend fun addCab1(){
        viewModel.addCab_toUser().collect { state->
            when(state){
                is State.Loading ->{
                    binding.addCabNext.text = "Loading"
                }
                is State.Success -> {
                    delay(500)
                    binding.addCabNext.text = "Loading."
                    addCab2()
                }
                is State.Failed -> {
                    if(state.message=="postRef must not be null"){
                        delay(500)
                        binding.addCabNext.text = "Loading."
                        addCab2()
                    }
                    else{
                        showToast(state.message)
                        Log.d("Wowtag","Failed 1 : "+state.message)
                        binding.addCabNext.isEnabled = true
                        binding.addCabBack.isEnabled = true
                    }
                }
            }
        }
    }
    private suspend fun addCab2(){
        viewModel.addCab_toCloud().collect { state->
            when(state){
                is State.Loading ->{
                    binding.addCabNext.text = "Loading.."
                }
                is State.Success -> {
                    delay(500)
                    binding.addCabNext.text = "Success"
                    viewModel.remove4()
                    delay(250)
                    activity?.onBackPressed()
                }
                is State.Failed -> {
                    if(state.message=="postRef must not be null"){
                        delay(500)
                        binding.addCabNext.text = "Success"
                        viewModel.remove4()
                        delay(250)
                        activity?.onBackPressed()
                    }
                    else{
                        showToast(state.message)
                        Log.d("Wowtag","Failed 2 : "+state.message)
                        binding.addCabNext.isEnabled = true
                        binding.addCabBack.isEnabled = true
                    }
                }
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO DESTROY LATER
    }


}
