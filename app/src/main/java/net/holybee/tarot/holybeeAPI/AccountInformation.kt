package net.holybee.tarot.holybeeAPI


import android.app.Application
import android.content.Context
import android.util.Log
private const val TAG = "AccountInformation"

object AccountInformation {

    var authToken = ""
    var username = ""
    var email = ""
    var password = ""
    var coins = 0


    val isLoggedIn:Boolean
        get() {
        if (authToken.length > 20) return true
        else return false}

    fun save(application: Application) {
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString("authToken", authToken)
            putString("username", username)
            putString("password", password)
            putString("email", email)
            putInt("coins", coins)
            apply()
        }
    }

    fun saveAuthToken(application: Application, newAuthToken:String) {
        Log.d(TAG,"Saving token: $newAuthToken")
        authToken = newAuthToken
        save(application)
    }

    fun readAuthToken(application: Application) {
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        val defaultValue = ""
        val newUsername = preferences.getString("username", defaultValue)
        val newPassword = preferences.getString("password", defaultValue)
        val newEmail = preferences.getString("email", defaultValue)
        val newCoins = preferences.getInt("coins", 0)
        username = newUsername ?: ""
        password = newPassword ?: ""
        email = newEmail ?: ""
        coins = newCoins
        }

    fun logout (application: Application) {
        authToken = ""
        username = ""
        email = ""
        password = ""
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString("authToken", authToken)
            apply()
        }
        Log.i(TAG,"Logged out of API.")

    }
}