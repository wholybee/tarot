/*
 * Copyright 2022 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.holybee.tarot.googleBilling.repository

import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import net.holybee.tarot.googleBilling.BillingClientWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


/**
 * The [PurchaseDataRepository] processes and transforms the [StateFlow] data received from
 * the [BillingClientWrapper] into [Flow] data available to the viewModel.
 *
 */
const val TAG = "Data Repository"
class PurchaseDataRepository(billingClientWrapper: BillingClientWrapper) {


    // Set to true when a returned purchase is a coin.
    val hasCoin: Flow<Boolean> = billingClientWrapper.inappPurchases.map { purchaseList ->
        purchaseList.any { purchase ->
            purchase.products.contains(COIN_INAPP)
        }
    }

    // ProductDetails for the coin.
    val coinProductDetails: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.flatMapConcat { map ->
          map.values.asFlow()
        }
            /*
                val coinProductDetails: Flow<ProductDetails> =
                    billingClientWrapper.productWithProductDetails.filter { productDetails ->
                        Log.d(TAG,productDetails.toString())
                        LIST_OF_INAPP.any { key ->
                            productDetails.containsKey(
                                key
                            )
                        }
                    }.map { it[COIN_INAPP]!! } */


    val inappPurchaseFlow: StateFlow<List<Purchase>> = billingClientWrapper.inappPurchases



    companion object {
        // List of subscription product offerings

        private const val COIN_INAPP = "coin"
        private const val COIN_250 = "coin250"
        private val LIST_OF_INAPP = listOf(COIN_INAPP, COIN_250)
    }
}
