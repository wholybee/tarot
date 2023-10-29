package net.holybee.tarot

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import net.holybee.tarot.googleBilling.Constants.MAX_CURRENT_PURCHASES_ALLOWED
import net.holybee.tarot.googleBilling.BillingClientWrapper
import net.holybee.tarot.googleBilling.repository.PurchaseDataRepository

private const val TAG = "PurchaseViewModel"

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    // use the billing client and billing repository
    private var billingClient: BillingClientWrapper = BillingClientWrapper(application)
    private var repo: PurchaseDataRepository =
        PurchaseDataRepository(billingClientWrapper = billingClient)
    private val _billingConnectionState = MutableLiveData(false)


    val hasCoinFlow = repo.hasCoin
    val coinsForSaleFlow = repo.coinProductDetails
    val inappPurchaseFlow = repo.inappPurchaseFlow

    // Start the billing connection when the viewModel is initialized.
    init {
        billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
    }
/// Coins are now consumed in onPurcahsesUpdates in BillingClient
  /*  fun consumePurchaseOnServer (purchase: Purchase?) {

        billingClient.consumePurchaseOnServer(purchase)
    } */

    fun buyCoin(
        productDetails: ProductDetails,
        currentPurchases: List<Purchase>?,
        activity: Activity,
        tag: String
    ) {
        // Get current purchase. In this app, a user can only have one current purchase at
        // any given time.
        if (currentPurchases == null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )

            val billingParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(
                activity,
                billingParams
            )
        } else if (currentPurchases.isNotEmpty() &&
            (currentPurchases.size > MAX_CURRENT_PURCHASES_ALLOWED)
        ) {
            // The developer has allowed users  to have more than 1 purchase, so they need to
            /// implement a logic to find which one to use.
            Log.d(TAG, "User has more than 1 current purchase.")
        }
    }

}