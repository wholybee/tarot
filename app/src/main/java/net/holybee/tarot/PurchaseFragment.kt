package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import net.holybee.tarot.googleBilling.Constants.COIN_TAG
import net.holybee.tarot.databinding.FragmentPurchaseBinding
import net.holybee.tarot.holybeeAPI.AccountInformation

private const val TAG = "PurchaseFragment"
class PurchaseFragment : Fragment() {

    private lateinit var viewModel: PurchaseViewModel

    private var _binding: FragmentPurchaseBinding? = null
    private val binding
        get()=checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[PurchaseViewModel::class.java]

        //  Use the ViewModel

        updateCoinCount()


        lifecycleScope.launch {
            viewModel.productDetailsListStateFlow.collect {
                val buttonContainer = binding.buttonContainer
                val themedContext = ContextThemeWrapper(requireContext(),R.style.AppTheme)
                it.forEach { product ->

                    val button = Button(themedContext)
                    button.text = getString(
                        R.string.purchase_button,
                        product.title,
                        product.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    )


                    button.setOnClickListener {
                        clickBuyCoin(product)
                    }
                    buttonContainer.addView(button)
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        }


    private fun updateCoinCount () {
        val coinText = "Coins: ${AccountInformation.coins}"
        Log.i(TAG,coinText)
        binding.coinsTextView.text = coinText
    }

    private fun clickBuyCoin (product: ProductDetails) {

        lifecycleScope.launch {

                viewModel.buyCoin(
                    productDetails = product,
                    currentPurchases = null,
                    tag = product.productId,
                    activity = requireActivity()
                )

        }

    }
}