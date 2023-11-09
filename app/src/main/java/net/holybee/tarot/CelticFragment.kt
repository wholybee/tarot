package net.holybee.tarot

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentCelticBinding
import net.holybee.tarot.databinding.FragmentTarotQuestionBinding

class CelticFragment : Fragment() {

    private var _binding: FragmentCelticBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    companion object {
        fun newInstance() = CelticFragment()
    }

    private lateinit var viewModel: CelticViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCelticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CelticViewModel::class.java)
        
        if (viewModel.gamePlay==GamePlay.NOTDEALT) {
            showCardsFaceDown()
        } else {
            showCardsFaceUp()
        }

        binding.dealButton2.setOnClickListener {
            clickDealButton()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tarot_question, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.open_account -> {
                findNavController().navigate(
                    CelticFragmentDirections.actionToAccountFragment())
                true
            }
            R.id.open_buyCoins -> {
                navigatePurchase()
                true
            }
            R.id.rate_app -> {
                rateApp()
                true
            }
            else->super.onOptionsItemSelected(item)
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
            binding.card1View.let {
                it.contentDescription = viewModel.hand[0]?.text
                setCardPicture(requireContext(), it, viewModel.hand[0]?.filename)
            }
            delay(100)
            binding.card2View.let {
                it.contentDescription = viewModel.hand[1]?.text
                setCardPicture(requireContext(), it, viewModel.hand[1]?.filename)
            }
            delay(100)
            binding.card3View.let {
                it.contentDescription = viewModel.hand[2]?.text
                setCardPicture(requireContext(), it, viewModel.hand[2]?.filename)
            }
            delay(100)
            binding.card4View.let {
                it.contentDescription = viewModel.hand[3]?.text
                setCardPicture(requireContext(), it, viewModel.hand[3]?.filename)
            }
            delay(100)
            binding.card5View.let {
                it.contentDescription = viewModel.hand[4]?.text
                setCardPicture(requireContext(), it, viewModel.hand[4]?.filename)
            }
            delay(100)
            binding.card6View.let {
                it.contentDescription = viewModel.hand[5]?.text
                setCardPicture(requireContext(), it, viewModel.hand[5]?.filename)
            }
            delay(100)
            binding.card7View.let {
                it.contentDescription = viewModel.hand[6]?.text
                setCardPicture(requireContext(), it, viewModel.hand[6]?.filename)
            }
            delay(100)
            binding.card8View.let {
                it.contentDescription = viewModel.hand[7]?.text
                setCardPicture(requireContext(), it, viewModel.hand[7]?.filename)
            }
            delay(100)
            binding.card9View.let {
                it.contentDescription = viewModel.hand[8]?.text
                setCardPicture(requireContext(), it, viewModel.hand[8]?.filename)
            }
            delay(100)
            binding.card10View.let {
                it.contentDescription = viewModel.hand[9]?.text
                setCardPicture(requireContext(), it, viewModel.hand[9]?.filename)
            }
        }

    }

    fun clickDealButton () {
        viewModel.deal()
        viewModel.gamePlay=GamePlay.DEALT
        showCardsFaceUp()

    }

}