package net.holybee.tarot.holybeeAPI


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "AccountInformation"

object AccountInformation {

    var authToken = ""
    var username = ""
    var email = ""
    var password = ""
    val coins = MutableLiveData<Int>()
    var ratingCount = 0
    var grantedPurchases : MutableList<String> = mutableListOf()

    val isLoggedIn:Boolean
        get() {
            return authToken.length > 20
        }

    fun save(application: Application) {
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString("authToken", authToken)
            putString("username", username)
            putString("password", password)
            putString("email", email)
            putInt("coins", coins.value ?: 0  )
            putInt("count", ratingCount)
            apply()
        }
    }

    fun saveLoginInfo(application: Application, newAuthToken:String) {
        Log.i(TAG,"Saving Account info for token: $newAuthToken")
        authToken = newAuthToken
        save(application)
    }

    fun readLoginInfo(application: Application) {
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        val defaultValue = ""
        val newUsername = preferences.getString("username", defaultValue)
        val newPassword = preferences.getString("password", defaultValue)
        val newEmail = preferences.getString("email", defaultValue)
        val newCoins = preferences.getInt("coins", 0)
        val newCount = preferences.getInt("count", 0)
        username = newUsername ?: ""
        password = newPassword ?: ""
        email = newEmail ?: ""
        coins.postValue(newCoins)
        ratingCount = newCount
        }

    fun logout (application: Application) {
        authToken = ""
        username = ""
        email = ""
        password = ""
        coins.postValue(0)
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString("authToken", authToken)
            putString("username", username)
            putString("password", password)
            putString("email", email)
            putInt("coins", coins.value ?: 0)
            apply()
        }
        Log.i(TAG,"Logged out of API.")

    }
}