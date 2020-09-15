package com.wilson.mvvmphone.repositories

import android.content.SharedPreferences
import com.google.gson.Gson
import com.wilson.mvvmphone.models.Users
import javax.inject.Inject

class SharedPreferenceManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    private val editor = sharedPreferences.edit()
    private val gson = Gson()
    fun addCurrentUser(user: Users){
        editor.putString("currentUser",gson.toJson(user))
        editor.commit()
    }

    fun getCurrentUser():Users? {
        return gson.fromJson(sharedPreferences.getString("currentUser",""),Users::class.java)
    }

    fun removeCurrentUser(){
        editor.remove("currentUser")
        editor.commit()
    }
}
