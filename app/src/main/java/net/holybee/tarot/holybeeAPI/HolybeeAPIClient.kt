package net.holybee.tarot.holybeeAPI

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

    fun getCoins(callback: GetCoinsResponseListener) {
        Log.i(TAG, "Get Coins")
        val query = JSONObject(
            mapOf(
                "username" to AccountInformation.username
            )
        )
        val authToken = AccountInformation.authToken
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postResponse =
                    client.post(HolybeeURL.getCoins) {
                        bearerAuth(authToken)
                        contentType(ContentType.Application.Json)
                        setBody(query.toString())
                    }
                val response = JSONObject(postResponse.body() as String)
                val status = response.get("responseCode").toString()
                if (status=="OK") {
                        val coins = response.get("coins").toString().toIntOrNull() ?: 0
                        callback.onGetCoinSuccess (coins)
                    } else {
                        callback.onGetCoinsFail(status)
                    }

            } catch (e:Exception) {
                Log.e(TAG,"GetCoins Fail: ${e.message}")
                callback.onGetCoinsFail(e.message ?: "Unknown Failure on getCoin.")
            }
        }
    }

    fun createAccountAsync(
        username: String,
        password: String,
        email: String,
        callback: CreateAccountResponseListener
    ) {
        val authToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NTFkMmE2ZGRkNmNlNGE1NjNjMzQwZTEiLCJpYXQiOjE2OTY0ODkwNTV9.8D184HxAAZ4oEH4lKXkW_My3w7CF4lDh5npJzuJHVMA"
        Log.i(TAG, "Create Account")
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
                        val coins = response.get("coins").toString().toIntOrNull() ?: 0
                        AccountInformation.coins = coins
                        AccountInformation.authToken = authToken

                        callback.onAccountCreateSuccess(authToken, coins)
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
        Log.i(TAG, "Login")
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
                        val coins = response.get("coins").toString().toIntOrNull() ?: 0
                        AccountInformation.coins = coins
                        AccountInformation.authToken = authToken
                        callback.onLoginSuccess(authToken, coins)
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

    fun grantUserPurchasedCoinsAsync(
        purchase: Purchase,
        callback: GrantUserPurchaseResponseListener
    ) {
        val purchaseToken = purchase.purchaseToken
        val authToken = AccountInformation.authToken
        val products = purchase.products
        for (product in products) {
            Log.i(TAG, product)
            val query = JSONObject(
                mapOf(
                    "purchaseToken" to purchaseToken,
                    "product" to product
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
                    if (responseCode == "OK") {
                        val coins = response.get("coins").toString().toIntOrNull() ?: 0
                        callback.onGrantSuccess(responseCode, purchase, coins)
                    } else {
                        callback.onGrantFail("Server Error")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error accessing Server API")
                    e.message?.let { callback.onGrantFail(it) }
                        ?: callback.onGrantFail("Unknown Failure")
                }
            } // coroutine scope

        } // for product

    } // consumePurcahseOnServer




} // Object HolybeeAIPCient









