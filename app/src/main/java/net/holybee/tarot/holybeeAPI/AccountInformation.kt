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

    val isLoggedIn:Boolean
        get() {
        if (authToken.length > 20) return true
        else return false}


    fun saveAuthToken(application: Application, newAuthToken:String) {
        Log.d(TAG,"Saving token: $newAuthToken")
        authToken = newAuthToken
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString("authToken", authToken)
            putString("username", username)
            putString("email", email)
            apply()
        }
    }

    fun readAuthToken(application: Application): String {
        val preferences = application.getSharedPreferences("login",Context.MODE_PRIVATE)
        val defaultValue = ""
        val newAuthToken = preferences.getString("authToken", defaultValue)
        val newUsername = preferences.getString("username", defaultValue)
        val newEmail = preferences.getString("email", defaultValue)
        authToken = newAuthToken ?: ""
        username = newUsername ?: ""
        email = newEmail ?: ""
        Log.d (TAG,"Read saved token: $newAuthToken")
        return authToken
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