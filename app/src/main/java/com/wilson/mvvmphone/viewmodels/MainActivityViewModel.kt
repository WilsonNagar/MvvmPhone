package com.wilson.mvvmphone.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.PhoneAuthProvider
import com.wilson.mvvmphone.base.BaseViewModel
import com.wilson.mvvmphone.interactors.MainActivityInteractor
import com.wilson.mvvmphone.models.Cabpools
import com.wilson.mvvmphone.models.ShopItems
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.models.Users
import com.wilson.mvvmphone.repositories.FireAuthRepository
import com.wilson.mvvmphone.repositories.FireStoreRepository
import com.wilson.mvvmphone.repositories.PhoneCallbacksListener
import com.wilson.mvvmphone.repositories.SharedPreferenceManager
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainActivityViewModel @Inject constructor(val firebaseAuthProvider : FireAuthRepository,val fireStoreProvider:FireStoreRepository,val sharedPreferenceManager: SharedPreferenceManager)
    : BaseViewModel<MainActivityInteractor>(), PhoneCallbacksListener {

    init {
        firebaseAuthProvider.setPhoneCallbacksListener(this)
        if (firebaseAuthProvider.isUserVerified()) {
            Log.d("Wow","Verified")
            //viewInteractor?.goToHomeFragment()
        }
        else{
            Log.d("Wow","Not Verified")
        }
    }

    var selectedPhoneNumber = MutableLiveData<String>()
    var selectedOtpNumber = MutableLiveData<String>()
    var currentFragment:Int = 0
    var currentState:Int = 0
    var currentSubState:Int = 1      //1 - signup , 0 - login
    var phone:String = ""
    var tempUser:Users = Users("","","","",0)
    //saved login/signupdata-------------------
    var tempGender:Int = 2
    var tempUsername1 = MutableLiveData<String>()
    var tempUsername2 = MutableLiveData<String>()
    var tempUserId = MutableLiveData<String>()
    //------------------------------------------
    fun checkCustomUser() = fireStoreProvider.checkUserExists(tempUserId.value.toString())
    fun addUsertoCloud() =  fireStoreProvider.addNewUser(Users(tempUserId.value.toString(),tempUsername1.value.toString(),tempUsername2.value.toString(),"+91"+selectedPhoneNumber.value.toString(),tempGender))
    fun sendOtpToPhone(phone: String) {
        this.phone = phone
        viewInteractor?.startSMSListener()
        firebaseAuthProvider.sendVerificationCode(phone)
    }
    fun verifyOtp() = firebaseAuthProvider.verityVerificationCode(selectedOtpNumber.value.toString())
    fun resendOtp() {
        viewInteractor?.startSMSListener()
        firebaseAuthProvider.resendCode(phone)
    }
    fun requestPhoneNumber(){
        viewInteractor?.requirePhone()
    }
    fun addUsertoDatabase(){
        sharedPreferenceManager.addCurrentUser(tempUser)
    }
    fun getUserfromDatabase(): Users? { return sharedPreferenceManager.getCurrentUser() }
    fun removeUserfromDatabase(){sharedPreferenceManager.removeCurrentUser()}
    fun clearViewModelData(){
        currentFragment=0
        currentState=0
        currentSubState=0
        phone=""
        tempUser = Users("","","","",0)
        tempGender = 2
        tempUsername1.value = ""
        tempUsername2.value = ""
        tempUserId.value = ""
    }
    fun signOutUser(){
        firebaseAuthProvider.firebaseAuth.signOut()
    }

    override fun onVerificationCompleted() {
        viewInteractor?.showSnackBarMessage("Verification Completed")
        //viewInteractor?.goToHomeFragment()
    }
    override fun onVerificationCodeDetected(code: String) {
        selectedOtpNumber.value = code
    }
    override fun onVerificationFailed(message: String) {
        viewInteractor?.showSnackBarMessage(message)
    }
    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
        viewInteractor?.showSnackBarMessage("OTP has sent")
    }

    //add_cab data==============================================================================
    var fromPlace = ""
    var toPlace = ""
    var peopleType = 0 // 0 = no selection , 1 = only gender , 2 = open for all
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
    var chosenDate = MutableLiveData<String>()
    var chosenTime = MutableLiveData<String>()
    var tempCab = Cabpools()
    fun compareTwoDates():Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        val d1 = dateFormat.format(calendar.time)
        val d2 = "$chosenDate $chosenTime"
        // 0 = no , 2 = yes , 1 = exception
        return try{
            if(dateFormat.parse(d1)!!.after(dateFormat.parse(d2))) 0 else 2
        } catch (e:ParseException){ 1 }
    }
    fun showCalenderDate(){
        viewInteractor?.setCalenderDate()
    }
    fun showCalenderTime(){
        viewInteractor?.setCalenderTime()
    }


    fun addCab_toUser(): Flow<State<Void>> {
        tempCab = Cabpools(fromPlace,toPlace,chosenTime.value,chosenDate.value,tempUser.fname+" "+tempUser.lname,tempUser.id,tempUser.gender)
        return fireStoreProvider.addCabpool_toUser(tempCab)
    }
    fun addCab_toCloud() = fireStoreProvider.addCabpool_toCloud(tempCab)
    fun deleteCab_fromUser() = fireStoreProvider.deleteCabpool_toUser(tempUser.id.toString())
    fun deleteCab_fromCloud(wowdate:String) = fireStoreProvider.deleteCabpool_toCloud(wowdate,tempUser.id.toString())

    fun remove4(){
        fromPlace = ""
        toPlace = ""
        peopleType = 0
        chosenTime.value = null
        chosenDate.value = null
        tempCab = Cabpools()
    }
    //====================================================================================================================================
    //====================================================================================================================================

    var activeCabpools = MutableLiveData<List<Cabpools>>()
    fun showCabpoolList(date:String) = fireStoreProvider.fetchCabpoolList(date)
    fun remove6(){
        activeCabpools = MutableLiveData()
        chosenDate.value = null
    }
    //====================================================================================================================================
    //====================================================================================================================================


    var chosenImage = MutableLiveData<Uri>()
    var isImageChosen:Boolean = false
    var chosenCategory:String =""
    var chosenItemName :String = ""
    var chosenPaymentNumber:MutableLiveData<Int> = MutableLiveData(1)
    var chosenItemPrice:Int = 0
    var itemNegotiable:Boolean = false
    var itemDescription:String = ""
    fun chooseImageNow(){
        viewInteractor?.selectImage()
    }
    fun shareImage(file : ByteArray) = fireStoreProvider.uploadImagetoCloud(file,chosenCategory)
    fun shareShopItem(finalImageUri:String): Flow<State<Int>> {
        val currentUser = getUserfromDatabase()!!
        val item = ShopItems(chosenItemName,chosenCategory,finalImageUri,itemDescription,"${currentUser.fname} ${currentUser.lname}",currentUser.id,currentUser.phno,chosenItemPrice,chosenPaymentNumber.value,itemNegotiable)
        return fireStoreProvider.sendShopItem(item)
    }
    fun remove5(){
        chosenImage = MutableLiveData()
        isImageChosen = false
        chosenCategory = ""
        chosenItemName = ""
        chosenItemPrice = 0
        chosenPaymentNumber.value = 1
        itemNegotiable = false
        itemDescription = ""
        fragmentState.value = 0
    }
    //====================================================================================================================================
    //====================================================================================================================================

    var chosenCategoryNum = 0
    var activeShopItems = MutableLiveData<List<ShopItems>>()
    var fragmentState = MutableLiveData<Int>()
    fun fakeitems():List<ShopItems> = listOf(ShopItems(), ShopItems(),ShopItems(),ShopItems())
    fun showItemsList(categoryname:String) = fireStoreProvider.fetchItemsList(categoryname)
    var selectedCallNumber:String = ""
    fun callSomeone(number:String){
        selectedCallNumber = number
        viewInteractor?.callPerson()
    }

    fun remove7(){
        chosenCategoryNum=0
        activeShopItems = MutableLiveData<List<ShopItems>>()
        selectedCallNumber = ""
    }

    //====================================================================================================================================
    var progressBarVisible = MutableLiveData<Boolean>(false)
    var alertBoxVisible = MutableLiveData<Boolean>(false)
    var blackScreenVisible = MutableLiveData<Boolean>(false)
    var alertBoxAnswer = MutableLiveData<Int>(0)
    var alertBoxTitle = ""
    var alertBoxMsg = ""
    //====================================================================================================================================
    fun BackPressed():Boolean{
        if(progressBarVisible.value==true){
            viewInteractor?.showSnackBarMessage("Wait while we complete few tasks")
            return false
        }
        if(currentFragment==4)//add cab fragment
            remove4()
        if(currentFragment==6)//cabpool fragment
            remove6()
        if(currentFragment==5)//add store fragment
            if(fragmentState.value!=0){
                fragmentState.value = fragmentState.value?.minus(1)
                return false
            }
            remove5()
        if(currentFragment==7){//store fragment
            if(fragmentState.value!=0){
                fragmentState.value = fragmentState.value?.minus(1)
                return false
            }
            remove7()
        }
        return true
    }


}