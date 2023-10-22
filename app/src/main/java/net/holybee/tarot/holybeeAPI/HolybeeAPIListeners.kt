package net.holybee.tarot.holybeeAPI

import com.android.billingclient.api.Purchase
import io.ktor.client.statement.HttpResponse

 interface LoginResponseListener {
     fun onLoginSuccess(userToken: String)
     fun onLoginFail(result: String)
 }

interface CreateAccountResponseListener {
    fun onAccountCreateSuccess(userToken: String)
    fun onAccountCreateFail(result: String)
}

interface ConsumePurchaseResponseListener {
    fun onConsumeSuccess (result: String, purchase: Purchase)
    fun onConsumeFail(result: String)
}