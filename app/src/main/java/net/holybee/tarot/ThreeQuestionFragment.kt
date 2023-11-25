package net.holybee.tarot

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentThreeQuestionBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.GetCoinsResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient
import kotlin.math.roundToInt

private const val TAG = "TarotQuestionFragment"

class ThreeQuestionFragment : Fragment(), GetCoinsResponseListener {


    private lateinit var viewModel: ThreeQuestionViewModel
    private val openAi = OpenAI_wlh
    private val modelIdFortune = "3cardreading"
    private val modelIdCard = "card"
    private val cardsPrompt = "The cards that are showing are:\n"
    private val cardPrompt = "The card is: "
    private val questionPrompt = "Question:\n"

    private var _binding: FragmentThreeQuestionBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val  coinsObserver = { coins:Int ->
        val coinsText = "Coins: $coins"
        binding.coinsTextView2.text = coinsText
    }

    private var cardWidth = 125
    private var cardHeight = 217



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calculateCardSize()
        _binding = FragmentThreeQuestionBinding.inflate(inflater, container, false)
        setCardSize()

        return binding.root
    }



    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ThreeQuestionViewModel::class.java]
        AccountInformation.coins.observe(viewLifecycleOwner, coinsObserver)

        if (AccountInformation.isLoggedIn) {
            val client = HolybeeAPIClient
            client.getCoins(this)
        }

            enableButtons()


        when (viewModel.gamePlay) {
            GamePlay.ASKED -> {
                binding.QuestionTextView.isEnabled = false
                asked()
            }
            GamePlay.DEALT -> {
                binding.QuestionTextView.isEnabled = false
                dealt()
            }
            GamePlay.NOTDEALT -> {
                binding.QuestionTextView.isEnabled = true
                notDealt()
            }
        }

        if (viewModel.hand[0] == null) {
            showCardsFaceDown()

        } else {
            showCardsFaceUp()
        }



        binding.QuestionTextView.onFocusChangeListener = OnFocusChangeListener { _, p1 ->
            if (p1) {
                Log.i(TAG, "Question Text View Focused")
            } else {
                // show edit text entered text when it unfocused
                Log.i(TAG, "Question Text View Lost Focus")

                // hide soft keyboard when edit text lost focus
                hideSoftKeyboard(binding.QuestionTextView)
            }
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


    override fun onResume() {
        super.onResume()
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(true)
        myActionbar?.show()

            if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
                Dialogs.showCustomDialog(requireActivity(),layoutInflater, getString(R.string.out_of_coins))

            } else {


                if (viewModel.justLaunched) {
                    viewModel.justLaunched = false
                    Dialogs.showCustomDialog(requireActivity(),layoutInflater, getString(R.string.concentrate))
                } else {
                    Log.i(TAG,"checking ratings ${AccountInformation.ratingCount}")
                    if (AccountInformation.ratingCount > 10 && !AccountInformation.hasRated) {
                        AccountInformation.ratingCount = 0
                        Dialogs.rateDialog(requireActivity(),layoutInflater,this)
                    }

                }

            }

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

            android.R.id.home-> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }


    private fun hideSoftKeyboard(editText: EditText){
        (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }




    private fun clickDealButton() {
            if (binding.QuestionTextView.text.length < 3) {
                Toast.makeText(requireContext(), getString(R.string.ask_a_question), Toast.LENGTH_LONG).show()
                return
            }
            viewModel.gamePlay = GamePlay.DEALT
            binding.QuestionTextView.clearFocus()
            viewModel.deal()
            showCardsFaceUp()
            dealt()

    }



    private fun clickAskButton() {
        if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
            Dialogs.showCustomDialog(requireActivity(),layoutInflater,getString(R.string.out_of_coins))
            return
        }

            openAi.clearHistory()
            viewModel.gamePlay = GamePlay.ASKED
            binding.QuestionTextView.clearFocus()
            binding.progressBar.visibility = View.VISIBLE

            binding.dealButton.isEnabled = false

            val content = cardsPrompt +
                    binding.cardOneView.contentDescription + ", " +
                    binding.cardTwoView.contentDescription + ", and " +
                    binding.cardThreeView.contentDescription + "\n" +
                    questionPrompt +
                    binding.QuestionTextView.text
            AccountInformation.ratingCount+=1
            lifecycleScope.launch {
                val response= openAi.askGPT(content, modelIdFortune)
                binding.progressBar.visibility = View.INVISIBLE
                if (response.status=="OK") {
                    AccountInformation.coins.postValue( AccountInformation.coins.value?.minus(1))
                    findNavController().navigate(
                        ThreeQuestionFragmentDirections.actionReadingDisplay(response.message)
                    )
                } else {
                    Toast.makeText(context,"Error:\n${response.message}",Toast.LENGTH_LONG).show()
                    clickPlayAgain()
                }
            }

    }

    private fun clickPlayAgain () {

            for (i in 0..2) {
                viewModel.hand[i] = null
            }
            binding.apply {
                QuestionTextView.isEnabled = true
                QuestionTextView.setText("")
                dealButton.isEnabled = true
            }
            openAi.clearHistory()
            viewModel.gamePlay = GamePlay.NOTDEALT
            notDealt()
            showCardsFaceDown()


    }


    private fun clickCardOne() {
        if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
            Dialogs.showCustomDialog(requireActivity(),layoutInflater,getString(R.string.out_of_coins))
            return
        }
        openAi.clearHistory()
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[0]
        if (card != null) showCard(card)

    }

    private fun clickCardTwo() {
        if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
            Dialogs.showCustomDialog(requireActivity(),layoutInflater,getString(R.string.out_of_coins))
            return
        }
        openAi.clearHistory()
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[1]
        if (card != null) showCard(card)
    }

    private fun clickCardThree() {
        if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
            Dialogs.showCustomDialog(requireActivity(),layoutInflater,getString(R.string.out_of_coins))
            return
        }
        openAi.clearHistory()
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[2]
        if (card != null) showCard(card)
    }

    private fun showCardsFaceDown () {

        binding.cardOneView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it , getString(R.string.card_back))
        }

        binding.cardTwoView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(requireContext(), it, getString(R.string.card_back))
        }

        binding.cardThreeView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture( requireContext(),  it, getString(R.string.card_back))
        }

        binding.cardOneTextView.text = getString(R.string.face_down)
        binding.cardOneTextView.visibility = View.INVISIBLE
        binding.cardOneRomanTextView.visibility = View.INVISIBLE
        binding.cardTwoTextView.text = getString(R.string.face_down)
        binding.cardTwoTextView.visibility = View.INVISIBLE
        binding.cardTwoRomanTextView.visibility = View.INVISIBLE
        binding.cardThreeTextView.text = getString(R.string.face_down)
        binding.cardThreeTextView.visibility = View.INVISIBLE
        binding.cardThreeRomanTextView.visibility = View.INVISIBLE
        binding.infoText.visibility = View.INVISIBLE
    }

    private fun showCardsFaceUp () {
        var delay: Long = 0
        if (viewModel.gamePlay==GamePlay.DEALT) delay = 200
    lifecycleScope.launch {
        binding.cardOneView.let {
            it.contentDescription = viewModel.hand[0]?.text
            setCardPicture(requireContext(), it, viewModel.hand[0]?.filename)
        }
        delay (delay)
        binding.cardTwoView.let {
            it.contentDescription = viewModel.hand[1]?.text
            setCardPicture(requireContext(), it, viewModel.hand[1]?.filename)
        }
        delay (delay)
        binding.cardThreeView.let {
            it.contentDescription = viewModel.hand[2]?.text
            setCardPicture(requireContext(), it, viewModel.hand[2]?.filename)
        }
    }

        binding.cardOneTextView.text = viewModel.hand[0]?.text
        binding.cardOneRomanTextView.text = viewModel.hand[0]?.roman
        binding.cardOneTextView.visibility = View.VISIBLE
        binding.cardOneRomanTextView.visibility = View.VISIBLE
        binding.cardTwoTextView.text = viewModel.hand[1]?.text
        binding.cardTwoRomanTextView.text = viewModel.hand[1]?.roman
        binding.cardTwoTextView.visibility = View.VISIBLE
        binding.cardTwoRomanTextView.visibility = View.VISIBLE
        binding.cardThreeTextView.text = viewModel.hand[2]?.text
        binding.cardThreeRomanTextView.text = viewModel.hand[2]?.roman
        binding.cardThreeTextView.visibility = View.VISIBLE
        binding.cardThreeRomanTextView.visibility = View.VISIBLE
        binding.infoText.visibility = View.VISIBLE
    }



    @Suppress("UNUSED_EXPRESSION")
    private fun disableAllButtons () {
        binding.apply {
            dealButton.setOnClickListener { null }

            cardOneView.setOnClickListener { null }
            cardTwoView.setOnClickListener { null }
            cardThreeView.setOnClickListener { null }
        }
    }

    private fun enableButtons () {
        binding.let {
            if (viewModel.gamePlay == GamePlay.NOTDEALT) it.dealButton.setOnClickListener { clickDealButton() }

            it.cardOneView.setOnClickListener { clickCardOne() }
            it.cardTwoView.setOnClickListener { clickCardTwo() }
            it.cardThreeView.setOnClickListener { clickCardThree() }

        }
    }

    private fun notDealt () {
        binding.apply {
            dealButton.text = getString(R.string.deal_cards)
            dealButton.setOnClickListener { clickDealButton() }
            binding.infoText.text = ""
        }
    }

    private fun dealt () {
        binding.apply {
            dealButton.text = getString(R.string.ask_question)
            dealButton.setOnClickListener { clickAskButton() }
            binding.dealButton.isEnabled = true
            binding.QuestionTextView.isEnabled = false
            binding.infoText.text = getString(R.string.click_on_a_card)

        }
    }

    private fun asked () {
        binding.apply {
            dealButton.text = getString((R. string.ask_another_question))
            dealButton.setOnClickListener { clickPlayAgain() }
            binding.dealButton.isEnabled = true
            binding.infoText.text = getString(R.string.after_reading_info)
        }
    }

    private fun showCard(card: Card?) {
        binding.progressBar.visibility = View.VISIBLE
        disableAllButtons()
        AccountInformation.ratingCount+=1

        lifecycleScope.launch {
            val response = openAi.askGPT(cardPrompt + (card?.text ?: ""), modelIdCard)
            binding.progressBar.visibility = View.INVISIBLE
            if (response.status=="OK") {
                AccountInformation.coins.postValue( AccountInformation.coins.value?.minus(1))
                findNavController().navigate(
                    ThreeQuestionFragmentDirections.actionViewCard(response.message,
                        card?.filename ?: ""
                    )
                )
            } else {
                Toast.makeText(context,"Error:\n${response.message}",Toast.LENGTH_LONG).show()
                clickPlayAgain()
            }
        }
    }


    private fun navigatePurchase () {
        findNavController().navigate(
            ThreeQuestionFragmentDirections.actionToPurchaseFragment())
    }

    private fun calculateCardSize() {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val dpHeight = displayMetrics.heightPixels / displayMetrics.density
        val w1 = (dpWidth / 3.14).roundToInt()
        val h1 = (w1 * 1.73).roundToInt()
        val h2 = (dpHeight / 3.75).roundToInt()
        val w2 = (h2 / 1.73).roundToInt()

        if (w1 < w2) {
            cardWidth = w1
            cardHeight = h1
            Log.i(TAG,"w1 < w2 Width: $w1  Height: $h1")
        } else {
            cardWidth = w2
            cardHeight = h2
            Log.i(TAG,"w2 < w1 Width: $w2  Height: $h2")
        }

    }

    private fun setCardSize() {
        val cardsList = listOf<ImageButton>(
            binding.cardOneView,
            binding.cardTwoView,
            binding.cardThreeView
        )
        val scale = resources.displayMetrics.density
        val newWidthInPixels = (cardWidth * scale + 0.5f).toInt()
        val newHeightInPixels = (cardHeight * scale + 0.5f).toInt()

        cardsList.forEach {
            val layoutParams = it.layoutParams
            layoutParams.width = newWidthInPixels
            layoutParams.height = newHeightInPixels
            it.layoutParams = layoutParams
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG,"onDestroy")
 //       (requireActivity() as AppCompatActivity)
  //          .supportActionBar?.hide()
        _binding = null
    }
}



