package net.holybee.tarot


import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentTarotQuestionBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.GetCoinsResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient


private const val TAG = "TarotQuestionFragment"

class TarotQuestionFragment : Fragment() {

    private val systemPromptFortune =
        "You are a fortune teller. \nYou will be asked a question. Tarot cards will be shown, and you will answer the question based on the cards.\n"
    private val systemPromptCard =
        "You are a fortune teller. You will be shown a Tarot card, and you will explain the meaning of the card.\n "
    private val cardsPrompt = "The cards that are showing are:\n"
    private val cardPrompt = "The card is: "
    private val questionPrompt = "Question:\n"

    private var _binding: FragmentTarotQuestionBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    companion object {
        fun newInstance() = TarotQuestionFragment()
    }

    private lateinit var viewModel: TarotQuestionViewModel
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarotQuestionBinding.inflate(inflater, container, false)


        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TarotQuestionViewModel::class.java)
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



        binding.QuestionTextView.onFocusChangeListener = OnFocusChangeListener { p0, p1 ->
            if (p1) {
                Log.d(TAG, "Focused")
            } else {
                // show edit text entered text when it unfocused
                Log.d(TAG, "Lost Focus")

                // hide soft keyboard when edit text lost focus
                hideSoftKeyboard(binding.QuestionTextView)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!AccountInformation.isLoggedIn) {
            Toast.makeText(context,"Please Login to Continue.", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                TarotQuestionFragmentDirections.actionToAccountFragment()
            )
        } else {
            /// getCoins
            val coinsText = "Coins: ${AccountInformation.coins}"
            binding.coinsTextView2.setText(coinsText)
        }

    }

    fun hideSoftKeyboard(editText: EditText){
        (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun clickDealButton() {
            if (binding.QuestionTextView.text.length < 3) {
                Toast.makeText(requireContext(), "Please ask a question before dealing the cards.", Toast.LENGTH_LONG).show()
                return
            }
            viewModel.gamePlay = GamePlay.DEALT
            binding.QuestionTextView.clearFocus()
            viewModel.deal()
            showCardsFaceUp()
            dealt()

    }



    fun clickAskButton() {

            viewModel.gamePlay = GamePlay.ASKED
            binding.QuestionTextView.clearFocus()
            binding.progressBar.visibility = View.VISIBLE

            binding.dealButton.isEnabled = false
    //        asked()
            val content = cardsPrompt +
                    binding.cardOneView.contentDescription + ", " +
                    binding.cardTwoView.contentDescription + ", and " +
                    binding.cardThreeView.contentDescription + "\n" +
                    questionPrompt +
                    binding.QuestionTextView.text

            lifecycleScope.launch {
                val response= askGPT(content, systemPromptFortune)
                binding.progressBar.visibility = View.INVISIBLE
                if (response.status=="OK") {
                    AccountInformation.coins = AccountInformation.coins - 1
                    findNavController().navigate(
                        TarotQuestionFragmentDirections.actionReadingDisplay(response.message)
                    )
                } else {
                    Toast.makeText(context,"Error:\n${response.message}",Toast.LENGTH_LONG).show()
                    clickPlayAgain()
                }
            }

    }

    fun clickPlayAgain () {

            for (i in 0..2) {
                viewModel.hand[i] = null
            }
            binding.apply {
                QuestionTextView.isEnabled = true
                QuestionTextView.setText("")
            }

            viewModel.gamePlay = GamePlay.NOTDEALT
            notDealt()
            showCardsFaceDown()


    }


    fun clickCardOne() {
        Log.d(TAG, "clickCardOne")
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[0]
        if (card != null) showCard(card)

    }

    fun clickCardTwo() {
        Log.d(TAG, "clickCardTwo")
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[1]
        if (card != null) showCard(card)
    }

    fun clickCardThree() {
        Log.d(TAG, "clickCardThree")
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[2]
        if (card != null) showCard(card)
    }

    fun showCardsFaceDown () {

        binding.cardOneView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(it, getString(R.string.card_back))
        }

        binding.cardTwoView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(it, getString(R.string.card_back))
        }

        binding.cardThreeView.let {
            it.contentDescription = getString(R.string.face_down)
            setCardPicture(it, getString(R.string.card_back))
        }

        binding.cardOneTextView.text = getString(R.string.face_down)
        binding.cardTwoTextView.text = getString(R.string.face_down)
        binding.cardThreeTextView.text = getString(R.string.face_down)
    }

    fun showCardsFaceUp () {
        binding.cardOneView.let {
            it.contentDescription = viewModel.hand[0]?.text
            setCardPicture(it, viewModel.hand[0]?.filename)
        }
        binding.cardTwoView.let {
            it.contentDescription = viewModel.hand[1]?.text
            setCardPicture(it, viewModel.hand[1]?.filename)
        }
        binding.cardThreeView.let {
            it.contentDescription = viewModel.hand[2]?.text
            setCardPicture(it, viewModel.hand[2]?.filename)
        }
        binding.cardOneTextView.text = viewModel.hand[0]?.text
        binding.cardTwoTextView.text = viewModel.hand[1]?.text
        binding.cardThreeTextView.text = viewModel.hand[2]?.text
    }



    fun disableAllButtons () {
        binding.apply {
            dealButton.setOnClickListener { null }

            cardOneView.setOnClickListener { null }
            cardTwoView.setOnClickListener { null }
            cardThreeView.setOnClickListener { null }
        }
    }

    fun enableButtons () {
        binding.let {
            if (viewModel.gamePlay == GamePlay.NOTDEALT) it.dealButton.setOnClickListener { clickDealButton() }

            it.cardOneView.setOnClickListener { clickCardOne() }
            it.cardTwoView.setOnClickListener { clickCardTwo() }
            it.cardThreeView.setOnClickListener { clickCardThree() }

        }
    }

    fun notDealt () {
        binding.apply {
            dealButton.text = getString(R.string.deal_cards)
            dealButton.setOnClickListener { clickDealButton() }
            binding.infoText.text = ""
        }
    }

    fun dealt () {
        binding.apply {
            dealButton.text = getString(R.string.ask_question)
            dealButton.setOnClickListener { clickAskButton() }
            binding.dealButton.isEnabled = true
            binding.QuestionTextView.isEnabled = false
            binding.infoText.text = getString(R.string.click_on_a_card)

        }
    }

    fun asked () {
        binding.apply {
            dealButton.text = getString((R. string.ask_another_question))
            dealButton.setOnClickListener { clickPlayAgain() }
            binding.dealButton.isEnabled = true
            binding.infoText.text = getString(R.string.after_reading_info)
        }
    }

    fun showCard(card: Card?) {
        binding.progressBar.visibility = View.VISIBLE
        disableAllButtons()
        lifecycleScope.launch {
            val response = askGPT(cardPrompt + (card?.text ?: ""), systemPromptCard)
            binding.progressBar.visibility = View.INVISIBLE
            if (response.status=="OK") {
                AccountInformation.coins = AccountInformation.coins -1
                findNavController().navigate(
                    TarotQuestionFragmentDirections.actionViewCard(response.message)
                )
            } else {
                Toast.makeText(context,"Error:\n${response.message}",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setCardPicture(imageButton: ImageButton, fileName: String?) {
        val assetManager = context?.assets
        Log.d(TAG, "filename: $fileName")

        if ((imageButton.tag != fileName) && (fileName != null)) {
            imageButton.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    assetManager,
                    fileName,
                    measuredView.width,
                    measuredView.height
                )
                if (scaledBitmap != null) {
                    imageButton.setImageBitmap(context?.let { toRoundCorner(it, scaledBitmap, 6f) })
                    imageButton.tag = fileName
                } else {
                    imageButton.setImageBitmap(null)
                    imageButton.tag = null
                }
            }
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
                    TarotQuestionFragmentDirections.actionToAccountFragment())
                true
            }
            R.id.open_buyCoins -> {
                findNavController().navigate(
                    TarotQuestionFragmentDirections.actionToPurchaseFragment())
                true
            }
            else->super.onOptionsItemSelected(item)
            }
        }


}



