package com.wilson.mvvmphone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentOnboardingBinding
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding,MainActivityViewModel>() {

    companion object {
        fun newInstance(): OnboardingFragment {
            val args = Bundle()
            val fragment = OnboardingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun layoutId(): Int = R.layout.fragment_onboarding
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onboardingGetstarted.setOnClickListener {
            if(binding.onBoardingMotionlayout.progress.equals(0.0f)||binding.onBoardingMotionlayout.progress.equals(1.0f)){
                it.findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToSignUpFragment())
            }
        }

    }


}