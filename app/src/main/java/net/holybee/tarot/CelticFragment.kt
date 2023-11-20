package net.holybee.tarot

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
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
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
    private var myActionbar: ActionBar? = null
    private val  coinsObserver = { coins:Int ->
        val coinsText = "Coins: $coins"
        binding.coinsTextView.text = coinsText
    }
    companion object {
        fun newInstance() = CelticFragment()
    }

    private lateinit var viewModel: CelticViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCelticBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(true)
        myActionbar?.show()
        setHasOptionsMenu(true)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CelticViewModel::class.java)

        AccountInformation.coins.observe(viewLifecycleOwner, coinsObserver)

        if (AccountInformation.isLoggedIn) {
            val client = HolybeeAPIClient
            client.getCoins(this)
        }

        if (viewModel.gamePlay==GamePlay.NOTDEALT) {
            binding.dealButton2.text = getString(R.string.deal_cards)
            showCardsFaceDown()
        } else {
            binding.dealButton2.text = getString(R.string.continue_)
            showCardsFaceUp()
        }

        binding.dealButton2.setOnClickListener {
            clickDealButton()
        }
    }

    override fun onGetCoinSuccess(coins: Int) {

        AccountInformation.coins.postValue(coins)
        val coinText = "Coins: ${AccountInformation.coins.value}"
        Log.i(TAG, coinText)
    }

    override fun onGetCoinsFail(result: String) {
        Log.e(TAG, "getCoins Failed $result")
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.child, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.open_buyCoins -> {
                navigatePurchase()
                true
            }
            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }
            android.R.id.home ->{
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        myActionbar?.show()
        if ((AccountInformation.coins.value?.compareTo(10) ?: 0) < 0) {
            Dialogs.showCustomDialog(requireActivity(),layoutInflater, "You do not have enough coins. A full celtic reading costs 10 total coins.")

        } else {


            if (viewModel.justLaunched) {
                viewModel.justLaunched = false
                Dialogs.showCustomDialog(requireActivity(),layoutInflater, "Clear your mind and click 'Deal' to begin your reading.")
            } else {
                Log.i(TAG,"checking ratings")
                if (AccountInformation.ratingCount > 10 && !AccountInformation.hasRated) {
                    AccountInformation.ratingCount = 0
                    Dialogs.rateDialog(requireActivity(),layoutInflater,this)
                }

            }

        }

    }

    private fun navigatePurchase () {
        findNavController().navigate(
            CelticFragmentDirections.actionToPurchaseFragment())
    }


    private fun showCardsFaceDown () {

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

    private fun showCardsFaceUp () {
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

    private fun clickDealButton () {

        when (viewModel.gamePlay) {
            GamePlay.NOTDEALT -> {
                viewModel.deal()
                viewModel.gamePlay = GamePlay.DEALT
                showCardsFaceUp()
                binding.dealButton2.text = getString(R.string.continue_)
                return
            }

            GamePlay.DEALT -> {
                Log.e(TAG, "handSerializable:${viewModel.hand.toTypedArray().size}")
                Log.e(TAG, "hand:${viewModel.hand.size}")
                if ((AccountInformation.coins.value?.compareTo(10) ?: 0) < 0) {
                    Dialogs.showCustomDialog(
                        requireActivity(),
                        layoutInflater,
                        "You do not have enough coins. A full celtic reading costs 10 total coins."
                    )
                    return
                }
            }

            GamePlay.ASKED -> {
                // Nothing to do here
            }
        }
        viewModel.gamePlay = GamePlay.ASKED
        findNavController().navigate(
            CelticFragmentDirections.actionCelticDisplay(viewModel.hand.toTypedArray())
        )

    }







    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.hide()
        AccountInformation.coins.removeObserver(coinsObserver)
        _binding = null
    }

}