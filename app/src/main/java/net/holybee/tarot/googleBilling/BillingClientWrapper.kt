package net.holybee.tarot.googleBilling

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.ConsumePurchaseResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient


/**
 * The [BillingClientWrapper] isolates the Google Play Billing's [BillingClient] methods needed
 * to have a simple implementation and emits responses to the data repository for processing.
 *
 */
class BillingClientWrapper(
    context: Context
) : PurchasesUpdatedListener, ProductDetailsResponseListener, ConsumePurchaseResponseListener {
    private var lastPurchase : Purchase? = null

    private val context = context
    private var isConsuming = false
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // New  ProductDetails
    private val _productWithProductDetails =
        MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productWithProductDetails =
        _productWithProductDetails.asStateFlow()


    // Current Purchases

    private val _inappPurchases =
        MutableStateFlow<List<Purchase>>(listOf())
    val inappPurchases = _inappPurchases.asStateFlow()

    // Tracks new purchases acknowledgement state.
    // Set to true when a purchase is acknowledged and false when not.
    private var _isNewPurchaseAcknowledged = false
    val isNewPurchaseAcknowledged = _isNewPurchaseAcknowledged


    // Establish a connection to Google Play.
    fun startBillingConnection(billingConnectionState: MutableLiveData<Boolean>) {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing response OK")
                    // The BillingClient is ready. You can query purchases and product details here
                    queryPurchases()
                    queryProductDetails()
                    billingConnectionState.postValue(true)
                } else {
                    handleBillingError( billingResult.responseCode, billingResult.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i(TAG, "Billing connection disconnected")
                startBillingConnection(billingConnectionState)
            }
        })
    }

    // Query Google Play Billing for existing purchases.
    // New purchases will be provided to PurchasesUpdatedListener.onPurchasesUpdated().
    fun queryPurchases() {

        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }

        // Query for existing inapp products that have been purchased.
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchaseList.isNotEmpty()) {
                    Log.d(TAG,"purchaseList is not empty")
                    purchaseList.forEach {
                        Log.d(TAG, it.purchaseToken)
                    }
                    _inappPurchases.value = purchaseList
                } else {
                    _inappPurchases.value = emptyList()
                }

            } else {
                Log.e(TAG, billingResult.debugMessage)
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    fun queryProductDetails() {

        val paramsBuilder = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()

        for (product in LIST_OF_INAPP) {

            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        billingClient.queryProductDetailsAsync(
            paramsBuilder.setProductList(productList).build(),
            this)

        }



    // [ProductDetailsResponseListener] implementation
    // Listen to response back from [queryProductDetails] and emits the results
    // to [_productWithProductDetails].
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                var newMap = emptyMap<String, ProductDetails>()
                if (productDetailsList.isEmpty()) {
                    Log.e(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly " +
                                "published in the Google Play Console.\n" +
                                debugMessage
                    )
                } else {
                    newMap = productDetailsList.associateBy {
                        it.productId
                    }

                }
                _productWithProductDetails.value = newMap
                Log.d(TAG,productWithProductDetails.toString())
            } else -> {
                handleBillingError(responseCode, debugMessage)
            }
        }
    }

    // Launch Purchase flow
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        } else {
            billingClient.launchBillingFlow(activity, params)
        }
    }

    // PurchasesUpdatedListener that helps handle new purchases returned from the API
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !purchases.isNullOrEmpty()
        ) {
            // Post new purchase List to _purchases
            _inappPurchases.value = purchases

            // Then, handle the purchases
            for (purchase in purchases) {
                acknowledgePurchases(purchase)
                consumePurchaseOnServer(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "User has cancelled")
        } else {
            // Handle any other error codes.
        }
    }

    // Perform new subscription purchases' acknowledgement client side.
    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(
                    params
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        _isNewPurchaseAcknowledged = true
                        Log.i(TAG, "Purchase acknowledged")
                    }
                }
            } else { //  acknowledged
                Log.i(TAG, "Purchase is already acknowledged")
            }
        }
    }

    /*
    fun consumePurchase(purchase: Purchase?) {
        if (isConsuming) {
            Log.i(TAG,"Already consuming a coin. Skipping for now.")
            return
        }
        isConsuming = true
        purchase?.let {
            val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    isConsuming  = false
                    Log.i(TAG,"Consume Successful")
                } else {
                    Log.e(TAG,"Consume Failure")
                    isConsuming = false
                    handleBillingError(billingResult.responseCode, billingResult.debugMessage)
                }
            }
        }
    }  */

    fun consumePurchaseOnServer(purchase: Purchase?) {
        if (isConsuming) {
            Log.i(TAG, "Already consuming a coin. Skipping for now.")
            return
        }
        isConsuming = true
        val client = HolybeeAPIClient
        if (purchase != null) {
            client.consumePurchaseOnServerAsync(purchase, this)
        }
    }

    override fun onConsumeSuccess(result: String, purchase: Purchase, coins: Int) {
        ////////////////// This block to be deleted after full server implementation
        purchase.let {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(it.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConsuming = false
                    queryPurchases()
                    Log.i(TAG,"Consume Successful")
                } else {
                    Log.e(TAG,"Consume Failure")
                    isConsuming = false
                    handleBillingError(billingResult.responseCode, billingResult.debugMessage)
                }
            }
        }

        queryPurchases()
        isConsuming = false
        Log.i(TAG, "Server Consume Successful")
        AccountInformation.coins = coins
    }

    override fun onConsumeFail(result: String) {
        Log.e(TAG,"Failure Consuming Purchase: $result")
    }


    fun handleBillingError(responseCode: Int, debugMessage: String) {
        var errorMessage = ""
        errorMessage = when (responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing service is currently unavailable. Please try again later."
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "An error occurred while processing the request. Please try again later."
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "This feature is not supported on your device."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own this item."
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "You do not own this item."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "This item is not available for purchase."
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Billing service has been disconnected. Please try again later."
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Billing service timed out. Please try again later."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Billing service is currently unavailable. Please try again later."
            BillingClient.BillingResponseCode.USER_CANCELED -> "The purchase has been canceled."
            else -> "An unknown error occurred."
        }
        Log.e("BillingError", errorMessage)
        Log.e(TAG, debugMessage )

    }


    // End Billing connection.
    fun terminateBillingConnection() {
        Log.i(TAG, "Terminating connection")
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "BillingClient"

        // List of inapp purchases
        private const val COIN_INAPP = "coin"
        private const val COIN_250 = "coin250"
        private val LIST_OF_INAPP = listOf(COIN_INAPP, COIN_250)

    }



}