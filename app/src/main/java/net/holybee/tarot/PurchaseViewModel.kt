package net.holybee.tarot

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.holybee.tarot.googleBilling.Constants.MAX_CURRENT_PURCHASES_ALLOWED
import net.holybee.tarot.googleBilling.BillingClientWrapper
import net.holybee.tarot.holybeeAPI.AccountInformation

private const val TAG = "PurchaseViewModel"

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    // use the billing client and billing repository
    private var billingClient: BillingClientWrapper = BillingClientWrapper(application)
    private val _billingConnectionState = MutableLiveData(false)

    val coins = AccountInformation.coins

    val productDetailsListStateFlow: StateFlow<List<ProductDetails>> = run {
        val productListStateFlow = MutableStateFlow<List<ProductDetails>>(emptyList())

        billingClient.productWithProductDetails.onEach { map ->
            val productList = map.values.toList()
            productListStateFlow.value = productList
        }.launchIn(viewModelScope)

        productListStateFlow
    }

    // Start the billing connection when the viewModel is initialized.
    init {
        billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
    }

    fun buyCoin(
        productDetails: ProductDetails,
        currentPurchases: List<Purchase>?,
        activity: Activity
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
            Log.e(TAG, "User has more than 1 current purchase.")
        }
    }
    companion object {
        // List of subscription product offerings

        private const val COIN_INAPP = "coin"
        private const val COIN_250 = "coin250"
        private val LIST_OF_INAPP = listOf(COIN_INAPP, COIN_250)
    }
}