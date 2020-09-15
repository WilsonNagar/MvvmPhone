package com.wilson.mvvmphone.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.wilson.mvvmphone.R
import com.wilson.mvvmphone.ShopItemsListAdapter
import com.wilson.mvvmphone.base.BaseFragment
import com.wilson.mvvmphone.databinding.FragmentCategoryBinding
import com.wilson.mvvmphone.models.ShopItems
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class CategoryFragment : BaseFragment<FragmentCategoryBinding, MainActivityViewModel>(),ShopItemsListAdapter.OnItemClickListener {

    companion object {
        fun newInstance(): CategoryFragment {
            val args = Bundle()
            val fragment = CategoryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //var testingState:Int = 0
    override fun layoutId(): Int = R.layout.fragment_category
    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java

    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val recyclerViewAdapter: ShopItemsListAdapter = ShopItemsListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStartData()
        setupClickListeners()
    }

    private fun setupStartData(){
        binding.categoryShopitems.setHasFixedSize(true)
        binding.categoryShopitems.layoutManager = GridLayoutManager(this.requireContext(), 2)
        recyclerViewAdapter.setOnItemClickListner(this)
        binding.categoryShopitems.adapter = recyclerViewAdapter

        viewModel.currentFragment=7

        viewModel.activeShopItems.observe(requireActivity(),
            Observer<List<ShopItems>> { value ->
                recyclerViewAdapter.submitList(value)
            })

        viewModel.fragmentState.observe(requireActivity(),
            Observer<Int> { value ->
                //Log.d("C State: ",value.toString())
                when(value){
                    0 ->{
                        binding.categoryMotionlayout.transitionToState(R.id.category_set1)
                        binding.categoryTitlename.text = "Categories"
                    }
                    1->  {
                        binding.categoryMotionlayout.transitionToState(R.id.category_set2)
                        binding.categoryTitlename.text = getCategoryString(viewModel.chosenCategoryNum)
                    }
                    2->{
                        binding.categoryMotionlayout.transitionToState(R.id.category_set3)
                    }
                }
            })


    }
    private fun setupClickListeners() {
        with(binding){
            categoryCard1.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(1)) }
            }
            categoryCard2.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(2)) }
            }
            categoryCard3.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(3)) }
            }
            categoryCard4.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(4)) }
            }
            categoryCard5.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(5)) }
            }
            categoryCard6.setOnClickListener {
                disableAllCategory()
                viewModel.activeShopItems.value = viewModel.fakeitems()
                uiScope.launch { getItemList(getCategoryString(6)) }
            }
            categoryTopGraphic.setOnClickListener {
                viewModel.BackPressed()
            }
            categoryTitlename.setOnClickListener {
                //Log.d("Test State : ",testingState.toString())
            }
        }
    }

    private suspend fun getItemList(categoryString:String) {
        viewModel.showItemsList(categoryString).collect {state->
            when(state){
                is State.Loading ->{
                    Log.d("wow data loading : ","shopitems")
                }
                is State.Success ->{
                    Log.d("State Success :","")
//                    val tlist = ArrayList<ShopItems>()
//                    tlist.add(ShopItems())
//                    viewModel.activeShopItems.value = tlist
                    viewModel.chosenCategoryNum = getCategoryNumber(categoryString)
                    viewModel.fragmentState.value = 1
                    delay(50)
                    animateToState2()
                    Log.d("wow data item Success:","")
                    delay(2000)
                    viewModel.activeShopItems.value = state.data
                    enableAllCategory()
                }
                is State.Failed ->{
                    if(state.message == "empty"){
                        //TODO SHOW NO ITEMS PRESENT
                        enableAllCategory()
//                        val tlist = ArrayList<ShopItems>()
//                        tlist.add(ShopItems())
//                        viewModel.activeShopItems.value = tlist
                        Log.d("wow data item failed : ","empty")
                    }
                    else{
                        enableAllCategory()
                        //TODO WHEN SOME OTHER ERROR
                        showSnackBar(state.message)
                        Log.d("wow data failed : ","msg : "+state.message)
                    }
                }
            }
        }
    }

    private fun animateToState2() {
        if((binding.categoryMotionlayout.progress.equals(0.0f) || binding.categoryMotionlayout.progress.equals(1.0f))){
            binding.categoryMotionlayout.transitionToState(R.id.category_set2)
        }
    }
    private fun animateToState1() {
        if((binding.categoryMotionlayout.progress.equals(0.0f) || binding.categoryMotionlayout.progress.equals(1.0f))){
            binding.categoryMotionlayout.transitionToState(R.id.category_set1)
        }
    }

    fun getCategoryString(num : Int):String{
        return when(num){
            1 -> "Electronics"
            2 -> "Vehicles"
            3 -> "Books"
            4 -> "Sports"
            5 -> "Accomodations"
            6 -> "Misc"
            else -> ""
        }
    }
    fun getCategoryNumber(string: String):Int{
        return when(string){
           "Electronics" -> 1
           "Vehicles"-> 2
           "Books"-> 3
           "Sports"-> 4
           "Accomodations"-> 5
           "Misc"-> 6
            else -> 0
        }
    }

    fun disableAllCategory(){
        binding.categoryCard1.isEnabled = false
        binding.categoryCard2.isEnabled = false
        binding.categoryCard3.isEnabled = false
        binding.categoryCard4.isEnabled = false
        binding.categoryCard5.isEnabled = false
        binding.categoryCard6.isEnabled = false
    }
    fun enableAllCategory(){
        binding.categoryCard1.isEnabled = true
        binding.categoryCard2.isEnabled = true
        binding.categoryCard3.isEnabled = true
        binding.categoryCard4.isEnabled = true
        binding.categoryCard5.isEnabled = true
        binding.categoryCard6.isEnabled = true
    }

    override fun onItemClick(shopitem: ShopItems?,itemDrawable:Drawable?) {
        if(itemDrawable!=null){
            viewModel.fragmentState.value = 2
            with(binding){
                categoryShopitemName.text = shopitem!!.itemname
                categoryShopitemImage.setImageDrawable(itemDrawable)
                categoryShopitemPrice.text = shopitem.price.toString()+" ₹"
                categoryShopitemNegotiable.text = if(shopitem.negotiable)"Negotiable" else "Fixed Price"
                categoryShopitemDescription.text = shopitem.description
                categoryShopitemSeller.text = shopitem.sellername
                categoryShopitemPaymentPaytm.visibility = if(shopitem.payment?.rem(2) == 0)View.VISIBLE else View.GONE
                categoryShopitemPaymentUpi.visibility = if(shopitem.payment?.rem(3) == 0)View.VISIBLE else View.GONE
                categoryShopitemPaymentCash.visibility = if(shopitem.payment?.rem(5) == 0)View.VISIBLE else View.GONE

                categoryShopitemContactCall.setOnClickListener {
                    viewModel.callSomeone(shopitem.sellerphone.toString())
                }
                categoryShopitemContactWhatsapp.setOnClickListener {
                    try{
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, I would like to know more about your prodect name '${shopitem.itemname.toString()}'.")
                        sendIntent.putExtra(
                            "jid",
                            "91"+shopitem.sellerphone + "@s.whatsapp.net"
                        ) //phone number without "+" prefix
                        sendIntent.setPackage("com.whatsapp")
                        startActivity(sendIntent);
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showSnackBar("You haven't installed WhatsApp.")
                    }
                }
            }
        }
        else{
            showSnackBar("Loading...Please Wait")
        }
    }

}

