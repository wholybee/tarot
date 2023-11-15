package net.holybee.tarot

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentCelticBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.GetCoinsResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient
private const val TAG = "CelticFragment"
class CelticFragment : Fragment(), GetCoinsResponseListener {

    private var _binding: FragmentCelticBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    companion object {
        fun newInstance() = CelticFragment()
    }

    lateinit var viewModel: CelticViewModel // by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCelticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CelticViewModel::class.java)

        if (AccountInformation.isLoggedIn) {
            val client = HolybeeAPIClient
            client.getCoins(this)
        }

        if (viewModel.gamePlay==GamePlay.NOTDEALT) {
            binding.dealButton2.text = "Deal"
            showCardsFaceDown()
        } else {
            binding.dealButton2.text = "Continue"
            showCardsFaceUp()
        }

        binding.dealButton2.setOnClickListener {
            clickDealButton()
        }
    }

    override fun onGetCoinSuccess(coins: Int) {

        AccountInformation.coins.postValue(coins)
        val coinText = "Coins: ${AccountInformation.coins}"
        Log.i(TAG, coinText)
    }

    override fun onGetCoinsFail(result: String) {
        Log.e(TAG, "getCoins Failed $result")
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.child, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.open_buyCoins -> {
                navigatePurchase()
                true
            }
            R.id.rate_app -> {
                rateApp()
                true
            }
            R.id.navigate_back ->{
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        if ((AccountInformation.coins.value?.compareTo(10) ?: 0) < 0) {
            showCustomDialog(text = "You do not have enough coins. A full celtic reading costs 10 total coins.")

        } else {


            if (viewModel.justLaunched) {
                viewModel.justLaunched = false
                showCustomDialog(text = "Clear your mind and click 'Deal' to begin your reading.")
            } else {

                if (listOf(10,25,50,75).contains(AccountInformation.ratingCount)) {
                    rateDialog()
                }

            }

        }

    }

    fun navigatePurchase () {
        findNavController().navigate(
            CelticFragmentDirections.actionToPurchaseFragment())
    }

    fun rateApp() {
        val appPackageName = "net.holybee.tarot"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    fun showCardsFaceDown () {

        binding.card1View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card2View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card3View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card4View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card5View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card6View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }
        binding.card7View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card8View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card9View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.card10View.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

    }

    fun showCardsFaceUp () {
        lifecycleScope.launch() {
            var delay: Long = 0
            if (viewModel.gamePlay == GamePlay.DEALT)  delay = 100

            binding.card1View.let {
                it.contentDescription = viewModel.hand[0].text
                setCardPicture(requireContext(), it, viewModel.hand[0].filename)
            }
            delay (delay)
            binding.card2View.let {
                it.contentDescription = viewModel.hand[1].text
                setCardPicture(requireContext(), it, viewModel.hand[1].filename)
            }
            delay (delay)
            binding.card3View.let {
                it.contentDescription = viewModel.hand[2].text
                setCardPicture(requireContext(), it, viewModel.hand[2].filename)
            }
            delay (delay)
            binding.card4View.let {
                it.contentDescription = viewModel.hand[3].text
                setCardPicture(requireContext(), it, viewModel.hand[3].filename)
            }
            delay (delay)
            binding.card5View.let {
                it.contentDescription = viewModel.hand[4].text
                setCardPicture(requireContext(), it, viewModel.hand[4].filename)
            }
            delay (delay)
            binding.card6View.let {
                it.contentDescription = viewModel.hand[5].text
                setCardPicture(requireContext(), it, viewModel.hand[5].filename)
            }
            delay (delay)
            binding.card7View.let {
                it.contentDescription = viewModel.hand[6].text
                setCardPicture(requireContext(), it, viewModel.hand[6].filename)
            }
            delay (delay)
            binding.card8View.let {
                it.contentDescription = viewModel.hand[7].text
                setCardPicture(requireContext(), it, viewModel.hand[7].filename)
            }
            delay (delay)
            binding.card9View.let {
                it.contentDescription = viewModel.hand[8].text
                setCardPicture(requireContext(), it, viewModel.hand[8].filename)
            }
            delay (delay)
            binding.card10View.let {
                it.contentDescription = viewModel.hand[9].text
                setCardPicture(requireContext(), it, viewModel.hand[9].filename)
            }
        }

    }

    fun clickDealButton () {

        if ((AccountInformation.coins.value?.compareTo(10) ?: 0) < 0) {
            showCustomDialog(text = "You do not have enough coins. A full celtic reading costs 10 total coins.")
        } else {

            when (viewModel.gamePlay) {
                GamePlay.NOTDEALT -> {
                    viewModel.deal()
                    viewModel.gamePlay = GamePlay.DEALT
                    showCardsFaceUp()
                    binding.dealButton2.setText("Continue")
                }

                else -> {
                    findNavController().navigate(
                        CelticFragmentDirections.actionCelticDisplay()
                    )
                }
            }
        }
    }
    private fun showCustomDialog(text: String) {
        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.question_dialog, null)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialogView)

        // Set the message and nextButton click listener
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialog_message)
        val acceptButton = dialogView.findViewById<Button>(R.id.accept_button)

        messageTextView.text = text

        val dialog = builder.create()

        acceptButton.setOnClickListener {
            // Perform any necessary actions when the user accepts
            // For example, you can close the dialog and continue your fragment logic
            dialog.dismiss()
            // Continue with your fragment logic here
        }

        dialog.show()
    }

    private fun rateDialog() {
        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rate_dialog, null)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialogView)

        // Set the message and nextButton click listener

        val acceptButton = dialogView.findViewById<Button>(R.id.accept_button)
        val declineButton = dialogView.findViewById<Button>(R.id.decline_button)

        val dialog = builder.create()

        acceptButton.setOnClickListener {
            AccountInformation.ratingCount = 100
            dialog.dismiss()
            rateApp()
        }
        declineButton.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }

}