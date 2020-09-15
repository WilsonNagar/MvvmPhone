package com.wilson.mvvmphone.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.wilson.mvvmphone.CabpoolListAdapter
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentCabpoolBinding
import com.wilson.mvvmphone.models.Cabpools
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CabpoolFragment : BaseFragment<FragmentCabpoolBinding, MainActivityViewModel>() {

    companion object {
        fun newInstance(): CabpoolFragment {
            val args = Bundle()
            val fragment = CabpoolFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun layoutId(): Int = R.layout.fragment_cabpool
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val recyclerViewAdapter:CabpoolListAdapter = CabpoolListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStartData()
        setupClickListeners()

    }

    private fun setupStartData(){
        binding.cabpoolListview.setHasFixedSize(true)
        binding.cabpoolListview.adapter = recyclerViewAdapter
        viewModel.currentFragment=6

        viewModel.activeCabpools.observe(requireActivity(),
            Observer<List<Cabpools>> { value ->
                recyclerViewAdapter.submitList(value)
            })

        if(viewModel.chosenDate.value==null)
            getCurrentDate()
        viewModel.chosenDate.observe(requireActivity(),
            Observer<String> { value ->
                binding.cabpoolDateText.text = value
            })

    }

    private fun setupClickListeners(){
        binding.cabpoolSearch.setOnClickListener {
            binding.cabpoolErrorImage.background = null
            uiScope.launch { getCabpoolList() }
        }
        binding.cabpoolDateBg.setOnClickListener { viewModel.showCalenderDate() }
        binding.cabpoolCalender.setOnClickListener {
            Log.d("Wow","Cabpools : "+viewModel.activeCabpools.value.toString()+" :: Date = "+viewModel.chosenDate.value)
        }
    }

    private suspend fun getCabpoolList(){
        viewModel.showCabpoolList(viewModel.chosenDate.value.toString()).collect { state->
            when(state){
                is State.Loading ->{
                    Log.d("wow data loading : ","ui")
                    binding.cabpoolSearchProgress.visibility = View.VISIBLE
                    binding.cabpoolMotionLayout.isEnabled = false
                }
                is State.Success ->{
                    viewModel.activeCabpools.value = state.data
                    Log.d("wow data success : "," got data")
                    delay(5000)
                    binding.cabpoolSearchProgress.visibility = View.INVISIBLE
                    binding.cabpoolMotionLayout.isEnabled = true
                    binding.cabpoolMotionLayout.setTransitionDuration(500)
                    binding.cabpoolMotionLayout.transitionToState(R.id.see_cabpool)
                    delay(500)
                    binding.cabpoolMotionLayout.setTransitionDuration(0)
                    //It can be then easily observed by observer when changed
                }
                is State.Failed ->{
                    if(state.message == "empty"){
                        //TODO SHOW NO ITEMS PRESENT

                        viewModel.activeCabpools.value = null
                        Log.d("wow data failed : ","empty")
                        delay(5000)
                        binding.cabpoolSearchProgress.visibility = View.INVISIBLE
                        binding.cabpoolMotionLayout.isEnabled = true
                        binding.cabpoolErrorImage.setBackgroundResource(R.drawable.no_internet_ghost)
                        binding.cabpoolMotionLayout.setTransitionDuration(500)
                        binding.cabpoolMotionLayout.transitionToState(R.id.search_cabpool)
                        delay(500)
                        binding.cabpoolMotionLayout.setTransitionDuration(0)
                    }
                    else{
                        //TODO WHEN SOME OTHER ERROR
                        showSnackBar(state.message)
                        Log.d("wow data failed : ","msg : "+state.message)
                    }
                }
            }
        }
    }

    private fun getCurrentDate(){
        val sdf = SimpleDateFormat("dd MMM yyyy")
        val curdate = sdf.format(Calendar.getInstance().time)
        viewModel.chosenDate.value = curdate
    }
}