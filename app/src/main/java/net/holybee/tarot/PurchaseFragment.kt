package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.launch
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

    private val  coinsObserver = { coins:Int ->
        val coinsText = "Coins: $coins"
        binding.coinsTextView.text = coinsText
    }
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(true)
        myActionbar?.show()
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[PurchaseViewModel::class.java]

        //  Use the ViewModel

       AccountInformation.coins.observe(viewLifecycleOwner, coinsObserver)



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

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.back_only, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.open_account -> {

                true
            }
            R.id.open_buyCoins -> {

                true
            }
            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        }

    override fun onDestroyView() {
        super.onDestroyView()
        AccountInformation.coins.removeObserver(coinsObserver)
    }


    private fun clickBuyCoin (product: ProductDetails) {

        lifecycleScope.launch {

                viewModel.buyCoin(
                    productDetails = product,
                    currentPurchases = null,
                    activity = requireActivity()
                )

        }

    }
}