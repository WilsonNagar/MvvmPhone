package com.wilson.mvvmphone.fragments

import  android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentAddStoreBinding
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.File


class AddStoreFragment : BaseFragment<FragmentAddStoreBinding, MainActivityViewModel>() {

    companion object {
        fun newInstance(): AddStoreFragment {
            val args = Bundle()
            val fragment = AddStoreFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun layoutId(): Int = R.layout.fragment_add_store
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStartData()
        setupClickListeners()
    }
    private fun setupStartData() {
        viewModel.currentFragment = 5

        viewModel.chosenImage.observe(requireActivity(),
            Observer<Uri?> { value ->
                if (viewModel.isImageChosen) {
                    binding.addStoreImageDisplay.setImageURI(value)
                    binding.addStoreImage.alpha = 0f
                } else {
                    binding.addStoreImage.alpha = 1f
                }
            })
        viewModel.fragmentState.observe(requireActivity(),
            Observer<Int> { value ->
                when (value) {
                    0 -> {
                        binding.addStoreMotionlayout.transitionToState(R.id.add_store_phase1)
                        binding.addStoreTitlename.text = "Item Information"
                        binding.addStoreTopGraphic.setBackgroundResource(R.drawable.graphic_add_store1)
                    }
                    1 -> {
                        binding.addStoreMotionlayout.transitionToState(R.id.add_store_phase2)
                        binding.addStoreTitlename.text = "Payment Details"
                        binding.addStoreTopGraphic.setBackgroundResource(R.drawable.graphic_add_store2)
                    }
                    2 -> {
                        binding.addStoreMotionlayout.transitionToState(R.id.add_store_phase3)
                        binding.addStoreTitlename.text = "Item Details"
                        binding.addStoreTopGraphic.setBackgroundResource(R.drawable.graphic_add_store3)
                    }
                    3 -> {
                        binding.addStoreMotionlayout.transitionToState(R.id.add_store_phase4)
                        binding.addStoreTitlename.text = "Image Upload"
                        binding.addStoreTopGraphic.setBackgroundResource(R.drawable.graphic_add_store4)
                    }
                }
            })
        viewModel.chosenPaymentNumber.observe(requireActivity(),
            Observer<Int> { value ->
                binding.addStorePaymentPaytm.alpha = if (value % 2 == 0) 1f else 0.3f
                binding.addStorePaymentUpi.alpha = if (value % 3 == 0) 1f else 0.3f
                binding.addStorePaymentCash.alpha = if (value % 5 == 0) 1f else 0.3f
            })
        viewModel.alertBoxAnswer.observe(requireActivity(),
            Observer<Int> { value ->
                when(value){
                    0->{}
                    1->{
                        sendImage1()
                        viewModel.progressBarVisible.value = true
                        viewModel.alertBoxAnswer.value = 0
                        Log.d("Alert box : ","fragment yes")
                    }
                    2->{
                        viewModel.alertBoxAnswer.value = 0
                        Log.d("Alert box : ","fragment no")
                        viewModel.blackScreenVisible.value = false
                    }
                }
            }
        )
    }
    private fun setupClickListeners(){
        binding.addStoreNext.setOnClickListener {
            if (!isAnimating()) {
                if (viewModel.fragmentState.value == 0) {
                    if(checkError1(binding.addStoreItemnameText.text.toString(),binding.addStoreCategoryText.text.toString())) {
                        viewModel.fragmentState.value = viewModel.fragmentState.value?.plus(1)
                    }
                } else if (viewModel.fragmentState.value == 1) {
                    if(checkError2()) {
                        viewModel.fragmentState.value = viewModel.fragmentState.value?.plus(1)
                    }
                } else if (viewModel.fragmentState.value == 2) {
                    if(checkError3()) {
                        viewModel.fragmentState.value = viewModel.fragmentState.value?.plus(1)
                    }
                } else if (viewModel.fragmentState.value == 3) {
                    if(viewModel.isImageChosen) {
                        viewModel.alertBoxTitle = "Are you sure?"
                        viewModel.alertBoxMsg = "Do you want to confirm selling your item '${viewModel.chosenItemName}'?"
                        viewModel.alertBoxVisible.value = true
                        viewModel.blackScreenVisible.value = true

                    }
                    else{
                        showSnackBar("Choose an Image")
                    }
                }
            }
        }
        binding.addStoreBack.setOnClickListener {
            if (!isAnimating()) {
                if (viewModel.fragmentState.value != 0) {
                    viewModel.fragmentState.value = viewModel.fragmentState.value?.minus(1)
                }
            }
        }
        binding.addStoreImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                } else {
                    choseImage()
                }
            } else {
                choseImage()
            }
        }
        binding.addStorePaymentPaytm.setOnClickListener {
            val pno = viewModel.chosenPaymentNumber.value!!.toInt()
            viewModel.chosenPaymentNumber.value = if(pno%2==0)pno/2 else pno*2
        }
        binding.addStorePaymentUpi.setOnClickListener {
            val pno = viewModel.chosenPaymentNumber.value!!.toInt()
            viewModel.chosenPaymentNumber.value = if(pno%3==0)pno/3 else pno*3
        }
        binding.addStorePaymentCash.setOnClickListener {
            val pno = viewModel.chosenPaymentNumber.value!!.toInt()
            viewModel.chosenPaymentNumber.value = if(pno%5==0)pno/5 else pno*5
        }
    }

    private fun choseImage() {
        viewModel.chooseImageNow()
    }

    private fun checkError3(): Boolean {
        val text = binding.addStoreDescriptionText.text.toString()
        if(text.split(" ").size<20){
            showSnackBar("Description must be atleast 20 words.")
        }
        else{
            var lines = ""
            for (i in 0 until binding.addStoreDescriptionText.layout.lineCount) {
                lines = lines.plus(text.substring(binding.addStoreDescriptionText.layout.getLineStart(i))).plus("\n")
            }
            viewModel.itemDescription = lines
            return true
        }
        return false
    }

    private fun checkError2(): Boolean {
        if(binding.addStorePrice.text.isNotEmpty() && binding.addStorePrice.text.matches("-?\\d+(\\.\\d+)?".toRegex())){
            if(viewModel.chosenPaymentNumber.value!=1){
                viewModel.chosenItemPrice = binding.addStorePrice.text.toString().toInt()
                return true
            }
            else{
                showSnackBar("Choose Payment Options")
            }
        }
        else{
            showSnackBar("Invalid Item Price")
        }
        return false
    }

    private fun checkError1(itemname: String, itemcategory: String): Boolean {
        if(itemname.length<3){
            showSnackBar("Invalid Item Name")
            return false
        }
        viewModel.chosenItemName = itemname
        viewModel.chosenCategory = itemcategory
        return true
    }

    private fun isAnimating():Boolean{
        return !(binding.addStoreMotionlayout.progress.equals(0.0f) || binding.addStoreMotionlayout.progress.equals(1.0f))
    }

    private suspend fun sendImage2(data:ByteArray) {
        viewModel.shareImage(data).collect { state ->
            when (state) {
                is State.Loading -> {
                    //TODO LOADING IMAGE SHARE
                    Log.d("SHARE :","LOADING...")
                }
                is State.Success -> {
                    Log.d("SHARE :","SUCCESS")
                    sendShopItem(state.data)
                }
                is State.Failed -> {
                    Log.d("SHARE :","ERROR : "+state.message)
                    viewModel.progressBarVisible.value = false
                    viewModel.blackScreenVisible.value = false
                }
            }

        }
    }


    private fun sendImage1() {
        uiScope.launch {
            val newFile = File(viewModel.chosenImage.value?.path.toString())
            val compressed = Compressor.compress(requireContext(), newFile) {
                default(200, 200, Bitmap.CompressFormat.JPEG, 80)
                size(50000)
            }
            val data = compressed.readBytes()
            sendImage2(data)
        }
    }

    private suspend fun sendShopItem(data: String) {
        viewModel.shareShopItem(data).collect{state->
            when (state) {
                is State.Loading -> {
                    //TODO LOADING SHOP ITEM SHARE
                    Log.d("SHARE2 :","LOADING...")
                }
                is State.Success -> {
                    when(state.data){

                        1 ->{Log.d("SHARE2 :","SUCCESS 1 ")}
                        2 ->{
                            Log.d("SHARE2 :","SUCCESS 2 ")
                            viewModel.progressBarVisible.value = false
                            viewModel.blackScreenVisible.value = false
                        }
                    }
                }
                is State.Failed -> {
                    Log.d("SHARE2 :","ERROR : "+state.message)
                    viewModel.progressBarVisible.value = false
                    viewModel.blackScreenVisible.value = false
                }
            }
        }
    }


}

