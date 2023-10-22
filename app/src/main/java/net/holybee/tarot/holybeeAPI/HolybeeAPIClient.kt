package net.holybee.tarot.holybeeAPI

import android.app.Application
import android.util.Log
import com.android.billingclient.api.Purchase
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONObject
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object HolybeeAPIClient {
    private const val TAG="holybeeAPIClient"
    private val client = HttpClient(CIO)

    fun logout (application: Application) {
        AccountInformation.logout(application)
    }

    fun createAccountAsync(
        username: String,
        password: String,
        email: String,
        callback: CreateAccountResponseListener
    ) {
        val authToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NTFkMmE2ZGRkNmNlNGE1NjNjMzQwZTEiLCJpYXQiOjE2OTY0ODkwNTV9.8D184HxAAZ4oEH4lKXkW_My3w7CF4lDh5npJzuJHVMA"
        Log.d(TAG, "Create Account")
        val query = JSONObject(
            mapOf(
                "username" to username,
                "password" to password,
                "email" to email
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postResponse =
                    client.post(HolybeeURL.create) {
                        bearerAuth(authToken)
                        contentType(ContentType.Application.Json)
                        setBody(query.toString())
                    }
                val response = JSONObject(postResponse.body() as String)
                val status = response.get("status").toString()
                when (status) {
                    "success" -> {
                        Log.i(TAG, "Account Creation Success.")
                        val authToken = response.get("token").toString()
                        callback.onAccountCreateSuccess(authToken)
                    }

                    "failed" -> {
                        callback.onAccountCreateFail(response.get("error").toString())
                    }

                    else -> {
                        callback.onAccountCreateFail(
                            "Unknown Error Creating Account: \n" +
                                    response.toString()
                        )
                    }
                }
            } catch (e: Exception) {
                callback.onAccountCreateFail("Account Create Failure: ${e.message}")
            }
        }

    }

    fun loginAsync(
        username: String,
        password: String,
        callback: LoginResponseListener
    ) {
        Log.d(TAG, "Login")
        val query = JSONObject(
            mapOf(
                "username" to username,
                "password" to password,

            )
        )
    println(query.toString())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postResponse =
                    client.post(HolybeeURL.login) {
                        contentType(ContentType.Application.Json)
                        setBody(query.toString())
                    }
                val response = JSONObject(postResponse.body() as String)
                val status = response.get("status").toString()
                when (status) {
                    "success" -> {
                        Log.i(TAG, "Login Success.")
                        val authToken = response.get("token").toString()
                        callback.onLoginSuccess(authToken)
                    }

                    "failed" -> {
                        callback.onLoginFail(response.get("error").toString())
                    }

                    else -> {
                        callback.onLoginFail(
                            "Unknown Error Logging in: \n" +
                                    response.toString()
                        )
                    }
                }
            } catch (e: Exception) {
                callback.onLoginFail("Login Failure: ${e.message}")
            }
        }

    }

    fun consumePurchaseOnServerAsync(
        purchase: Purchase,
        callback: ConsumePurchaseResponseListener
    ) {
        val purchaseToken = purchase.purchaseToken
        val authToken = AccountInformation.authToken
        val query = JSONObject(
            mapOf(
                "purchaseToken" to purchaseToken
            )
        )
        Log.i(TAG, "Starting https request")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postResponse =
                    client.post(HolybeeURL.consumePurchase) {
                        bearerAuth(authToken)
                        contentType(ContentType.Application.Json)
                        setBody(query.toString())
                    }
                Log.i(TAG, postResponse.body() as String)
                val response = JSONObject(postResponse.body() as String)
                val responseCode = response.get("responseCode").toString()
                if (responseCode=="OK") {
                    callback.onConsumeSuccess(responseCode, purchase)
                } else {
                    callback.onConsumeFail("Server Error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error accessing Server API")
                e.message?.let { callback.onConsumeFail(it) }
                    ?: callback.onConsumeFail("Unknown Failure")
            }
        }
    }
}









