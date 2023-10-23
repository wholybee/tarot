package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.first
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
            viewModel.hasCoinFlow
                .collect { coin ->
                    binding.hasCoinCheckbox.isChecked = coin
                    Log.i(TAG, "collect coin flow")
                    updateCoinCount()

                }
        }

        lifecycleScope.launch {
            viewModel.coinsForSaleFlow
                .collect { coin ->
                    binding.buyCoinButton.isEnabled = true

                }
        }
        lifecycleScope.launch {

                viewModel.inappPurchaseFlow
                    .collect {
                       val purchaseList = it
                       purchaseList.forEach { purchase: Purchase ->  viewModel.consumePurchaseOnServer(purchase)}
                    }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buyCoinButton.setOnClickListener {
            clickBuyCoin()
        }
    }

    private fun updateCoinCount () {
        val coinText = "Coins: ${AccountInformation.coins}"
        Log.d(TAG,coinText)
        binding.coinsTextView.setText(coinText)
    }

    private fun clickBuyCoin () {

        lifecycleScope.launch {
            viewModel.coinsForSaleFlow.first().let {
                viewModel.buyCoin(
                    productDetails = it,
                    currentPurchases = null,
                    tag = COIN_TAG,
                    activity = requireActivity()
                )
            }
        }

    }
}