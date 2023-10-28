package net.holybee.tarot.holybeeAPI

import com.android.billingclient.api.Purchase

interface LoginResponseListener {
     fun onLoginSuccess(userToken: String, coins:Int)
     fun onLoginFail(result: String)
 }

interface CreateAccountResponseListener {
    fun onAccountCreateSuccess(userToken: String, coins:Int)
    fun onAccountCreateFail(result: String)
}

interface ConsumePurchaseResponseListener {
    fun onConsumeSuccess (result: String, purchase: Purchase, coins: Int)
    fun onConsumeFail(result: String)
}

interface GetCoinsResponseListener {
    fun onGetCoinSuccess (coins: Int)
    fun onGetCoinsFail(result: String)
}