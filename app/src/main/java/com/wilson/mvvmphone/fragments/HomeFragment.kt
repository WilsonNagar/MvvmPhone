package com.wilson.mvvmphone.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentHomeBinding
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding, MainActivityViewModel>() {

    companion object {
        fun newInstance(): OnboardingFragment {
            val args = Bundle()
            val fragment = OnboardingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun layoutId(): Int = R.layout.fragment_home
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java
    private val uiScope = CoroutineScope(Dispatchers.Main)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCreateData()
        setupClicks()
        binding.homeUser.setOnClickListener { FirebaseAuth.getInstance().signOut()
            viewModel.removeUserfromDatabase()
            viewModel.clearViewModelData()
            Toast.makeText(requireContext(),"LOGOUT SUCCESS",Toast.LENGTH_LONG).show()
        }
    }
    private fun setupCreateData(){

        viewModel.tempUser = viewModel.getUserfromDatabase()!!
        Log.d("Details : ",viewModel.tempUser.toString())
        binding.homeUsername.text = viewModel.tempUser.fname
        binding.homeUser.setImageDrawable(ResourcesCompat.getDrawable(resources, if(viewModel.tempUser.gender==1)R.drawable.ic_boy else R.drawable.ic_girl, null))
        viewModel.currentState=0
        viewModel.currentFragment = 3

    }
    private fun setupClicks(){
        binding.homeCabpoolBackground.setOnClickListener {
            if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentState==0){
                    binding.mainMotionlayout.transitionToState(R.id.home_cab_scene)
                    viewModel.currentState=2

                    binding.homeCabpoolText.setText("Back")
                    binding.homeViewprofile.setText("Cabpool")
                }
                else if(viewModel.currentState==2){
//                    binding.dragImage.setImageDrawable(activity?.applicationContext?.let { it1 ->
//                        ContextCompat.getDrawable(
//                            it1, R.drawable.back_to_drag)
//                    })
//                    (binding.dragImage.drawable as AnimatedVectorDrawable).start()
                    binding.mainMotionlayout.transitionToState(R.id.home_normal_scene)
                    viewModel.currentState=0

                    binding.homeCabpoolText.setText("Cabpool")
                    binding.homeViewprofile.setText("View Profile")
                }
            }
        }
        binding.homeStoreBackground.setOnClickListener {
            //Log.d("Testing ",it.resources.getResourceName(it.id)+" clicked")
            if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentState==0){
                    binding.mainMotionlayout.transitionToState(R.id.home_store_scene)
                    viewModel.currentState=3

                    binding.homeStoreText.setText("Back")
                    binding.homeViewprofile.setText("Store")
                }
                else if(viewModel.currentState==3){
                    binding.mainMotionlayout.transitionToState(R.id.home_normal_scene)
                    viewModel.currentState=0

                    binding.homeStoreText.setText("Store")
                    binding.homeViewprofile.setText("View Profile")
                }
            }
        }
        binding.homeViewprofile.setOnClickListener{
            if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
                if(viewModel.currentState==0){
                    binding.mainMotionlayout.transitionToState(R.id.home_profile_scene)
                    viewModel.currentState = 1
                    binding.homeViewprofile.setText("Back")
                }
                else if(viewModel.currentState==1){
                    binding.mainMotionlayout.transitionToState(R.id.home_normal_scene)
                    viewModel.currentState = 0
                    binding.homeViewprofile.setText("View Profile")
                }
            }
        }
        binding.homeCabpoolOp2.setOnClickListener { toAddCabpool() }
        binding.homeCabpoolOp2Text.setOnClickListener { toAddCabpool() }
        binding.homeCabpoolOp3.setOnClickListener {
            predelete_on()
            uiScope.launch {
                deleteCab1()
            }
        }
        binding.homeCabpoolOp1.setOnClickListener { toSearchCabpool() }
        binding.homeStoreOp2.setOnClickListener {
            viewModel.fragmentState.value = 0
            toAddStore()
        }
        binding.homeStoreOp1.setOnClickListener {
            viewModel.fragmentState.value = 0
            toSeachStore()
        }

    }

    private fun toSeachStore() {
        if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
            if(viewModel.currentState==3){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment())
                //Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_addCabFragment,null)
            }
        }
    }

    fun toAddCabpool(){
        if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
            if(viewModel.currentState==2){
                  findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddCabFragment())
                //Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_addCabFragment,null)
            }
        }
    }
    fun toSearchCabpool(){
        if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
            if(viewModel.currentState==2){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCabpoolFragment())
                //Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_addCabFragment,null)
            }
        }
    }
    fun toAddStore(){
        if(binding.mainMotionlayout.progress.equals(0.0f)||binding.mainMotionlayout.progress.equals(1.0f)){
            if(viewModel.currentState==3){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddStoreFragment())
                //Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_addCabFragment,null)
            }
        }
    }
    private suspend fun deleteCab1(){
        viewModel.deleteCab_fromUser().collect { state->
            when(state){
                is State.Loading ->{
                    binding.homeCabpoolOp3Text.text = "Checking"
                }
                is State.Success -> {
                    if(state.data==""){
                        binding.homeCabpoolOp3Text.text = "No Active Cabpool"
                        Handler().postDelayed({
                            binding.homeCabpoolOp3Text.text = "DELETE"
                            predelete_off()
                        },1000)
                    }
                    else{
                        delay(500)
                        deleteCab2(state.data)
                    }
                }
                is State.Failed -> {
                    binding.homeCabpoolOp3Text.text = "Failed"
                    Log.d("WOW","Failed1 : "+state.message)
                    Handler().postDelayed({
                        binding.homeCabpoolOp3Text.text = "DELETE"
                        predelete_off()
                    },1000)
                }
            }
        }
    }
    private suspend fun deleteCab2(date:String){
        viewModel.deleteCab_fromCloud(date).collect { state->
            when(state){
                is State.Loading ->{
                    binding.homeCabpoolOp3Text.text = "Deleting"
                }
                is State.Success -> {
                    if(state.data==""){
                        binding.homeCabpoolOp3Text.text = "No Active Cabpool"
                        Handler().postDelayed({
                            binding.homeCabpoolOp3Text.text = "DELETE"
                            predelete_off()
                        },1000)
                    }
                    else{
                        binding.homeCabpoolOp3Text.text = "DELETED"
                        Handler().postDelayed({
                            binding.homeCabpoolOp3Text.text = "DELETE"
                            predelete_off()
                        },1000)
                    }
                }
                is State.Failed -> {
                    binding.homeCabpoolOp3Text.text = "Failed2"
                    Log.d("WOW","Failed2 : "+state.message)
                    Handler().postDelayed({
                        binding.homeCabpoolOp3Text.text = "DELETE"
                        predelete_off()
                    },1000)
                }
            }
        }
    }

    fun predelete_on(){
        with(binding){
            homeCabpoolOp1.isEnabled = false
            homeCabpoolOp1Text.isEnabled = false
            homeCabpoolOp2.isEnabled = false
            homeCabpoolOp2Text.isEnabled = false
            homeCabpoolOp3.isEnabled = false
            homeCabpoolOp3Text.isEnabled = false
            homeCabpoolBackground.isEnabled = false
        }
    }
    fun predelete_off(){
        with(binding){
            homeCabpoolOp1.isEnabled = true
            homeCabpoolOp1Text.isEnabled = true
            homeCabpoolOp2.isEnabled = true
            homeCabpoolOp2Text.isEnabled = true
            homeCabpoolOp3.isEnabled = true
            homeCabpoolOp3Text.isEnabled = true
            homeCabpoolBackground.isEnabled = true
        }
    }
}