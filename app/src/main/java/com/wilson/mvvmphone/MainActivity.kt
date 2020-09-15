package com.wilson.mvvmphone

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.wilson.mvvmphone.base.BaseActivity
import com.wilson.mvvmphone.databinding.ActivityMainBinding
import com.wilson.mvvmphone.interactors.MainActivityInteractor
import com.wilson.mvvmphone.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding,MainActivityViewModel>() ,
    MainActivityInteractor{

    companion object {
        public const val CREDENTIAL_PICKER_REQUEST = 1
        public const val SMS_CONSENT_REQUEST = 2

        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    override fun getViewModelClass(): Class<MainActivityViewModel> = MainActivityViewModel::class.java

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
                        } catch (e: ActivityNotFoundException) {
                            showSnackBar(e.message?: "Something went wrong")
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Time out occurred, handle the error.
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        viewModel.viewInteractor = this
        //if(firebaseAuth.currentUser!=null)firebaseAuth.signOut()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsVerificationReceiver, intentFilter)

        val navHostFragment = home_navhostfragment as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)
        val navController = navHostFragment.navController

        val destination = if (firebaseAuth.currentUser!=null) R.id.homeFragment else R.id.onboardingFragment
        navGraph.startDestination = destination
        navController.graph = navGraph

        setupStartData()
        setupOnClicks()

    }

    private fun setupOnClicks() {
        binding.homeAlertboxYes.setOnClickListener {
            viewModel.alertBoxAnswer.value = 1
            Log.d("Alert Box:","Yes Clicked")
            viewModel.alertBoxVisible.value = false
        }
        binding.homeAlertboxNo.setOnClickListener {
            viewModel.alertBoxAnswer.value = 2
            Log.d("Alert Box:","No Clicked")
            viewModel.alertBoxVisible.value = false
        }
        binding.homeBlackscreen.setOnClickListener {
            Log.d("Clicked","Black Screen")
        }
    }

    private fun setupStartData() {
        viewModel.progressBarVisible.observe(this,
            Observer<Boolean> { value ->
                if(value){
                    binding.homeProgressbar.visibility=View.VISIBLE
                }
                else{
                    binding.homeProgressbar.visibility=View.INVISIBLE
                }
            }
        )
        viewModel.blackScreenVisible.observe(this,
            Observer<Boolean> { value ->
                if(value){
                    binding.homeBlackscreen.visibility=View.VISIBLE
                    binding.homeContainerfrag.isEnabled = false
                }
                else{
                    binding.homeBlackscreen.visibility=View.INVISIBLE
                    binding.homeContainerfrag.isEnabled = true
                }
            }
        )
        viewModel.alertBoxVisible.observe(this,
            Observer<Boolean> { value ->
                if(value){
                    binding.homeAlertboxTitle.text = viewModel.alertBoxTitle
                    binding.homeAlertboxMsg.text = viewModel.alertBoxMsg
                    binding.homeAlertbox.visibility=View.VISIBLE
                }
                else{
                    viewModel.alertBoxTitle = ""
                    viewModel.alertBoxMsg = ""
                    binding.homeAlertbox.visibility=View.INVISIBLE
                }
            }
        )
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val credentialsClient = Credentials.getClient(this)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        startIntentSenderForResult(
            intent.intentSender,
            CREDENTIAL_PICKER_REQUEST,
            null, 0, 0, 0
        )
    }

    override fun showSnackBarMessage(message: String) {
        showSnackBar(message)
    }

    override fun startSMSListener() {
        val smsRetrieverClient = SmsRetriever.getClient(this)
        val task = smsRetrieverClient.startSmsUserConsent(null)
        task.addOnSuccessListener {
            Toast.makeText(this, "SMS Retriever starts", Toast.LENGTH_LONG).show()
        }
        task.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    override fun goToHomeFragment() {

//        val graphInflater = binding.homeNavhostfragment.findNavController().navInflater
//        val navGraph = graphInflater.inflate(R.navigation.nav_graph)
//        val navController = binding.homeNavhostfragment.findNavController()
//        val destination = if(FirebaseAuth.getInstance().currentUser!=null) R.id.homeFragment else R.id.onboardingFragment
//        navGraph.startDestination = destination
//        navController.graph = navGraph

    }

    override fun requirePhone() {
        requestHint()
    }

    override fun onDestroy() {
        unregisterReceiver(smsVerificationReceiver)
        super.onDestroy()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREDENTIAL_PICKER_REQUEST ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    val checkStringNum = credential?.id.toString()
                    viewModel.selectedPhoneNumber.value = checkStringNum.substring(checkStringNum.length-10)
                    Log.d("MyTag","Got "+viewModel.selectedPhoneNumber.value+" number")
                }

            SMS_CONSENT_REQUEST ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val oneTimeCode = message?.substring(0, 6)
                    //Timber.d("AuthActivity.onActivityResult message $oneTimeCode")
                    viewModel.selectedOtpNumber.value = oneTimeCode?.trim()
                }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.chosenImage.value = result.uri
                    viewModel.isImageChosen = true
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    Toast.makeText(
                        this,
                        "Crop Error : $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun setCalenderDate() {
        val mcurrentDate = Calendar.getInstance()
        val year = mcurrentDate[Calendar.YEAR]
        val month = mcurrentDate[Calendar.MONTH]
        val day = mcurrentDate[Calendar.DAY_OF_MONTH]
        val mDatePicker =
            DatePickerDialog(this, OnDateSetListener { datepicker, selectedyear, selectedmonth, selectedday ->
                    val sdf = SimpleDateFormat("dd MMM yyyy")
                    mcurrentDate.set(selectedyear,selectedmonth,selectedday)
                    val strDate = sdf.format(mcurrentDate.time)
                    viewModel.chosenDate.value = strDate
                }, day, month, year
            )
        //mDatePicker.setTitle("Please select date")
        mDatePicker.datePicker.minDate = System.currentTimeMillis()
        mDatePicker.show()

    }

    override fun selectImage() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(this)
    }

    override fun setCalenderTime(){
        val mcurrentDate = Calendar.getInstance()
        val hour = mcurrentDate[Calendar.HOUR_OF_DAY]
        val minute = mcurrentDate[Calendar.MINUTE]
        val mTimePicker = TimePickerDialog(this, OnTimeSetListener { timePicker, hr, mi ->
                val sdf = SimpleDateFormat("HH:mm:ss")
                mcurrentDate.set(2000,1,1,hr,mi,0)
                val strTime = sdf.format(mcurrentDate.time)
                viewModel.chosenTime.value = strTime
            }, hour, minute, false
        )

        //mTimePicker.setTitle("Please select time")
        mTimePicker.show()
    }



    override fun onBackPressed() {
        if(viewModel.BackPressed()) super.onBackPressed()
    }

    override fun callPerson() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CALL_PHONE),
                42)
            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.CALL_PHONE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed, we can request the permission.
//
//            }
        } else {
            // Permission has already been granted
            callPhone()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 42) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay!
                callPhone()
            } else {
                showSnackBarMessage("Permission not Granted")
            }
            return
        }
    }

    fun callPhone(){
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:+91 " + viewModel.selectedCallNumber))
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        startActivity(intent)
    }
}
