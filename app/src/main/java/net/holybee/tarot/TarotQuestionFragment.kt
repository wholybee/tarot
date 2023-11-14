package net.holybee.tarot

import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.net.Uri
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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentTarotQuestionBinding
import net.holybee.tarot.holybeeAPI.AccountInformation

private const val TAG = "TarotQuestionFragment"

class TarotQuestionFragment : Fragment() {
    private val openAi = OpenAI_wlh
    private val modelIdFortune = "3cardreading"
    private val modelIdCard = "card"
    private val cardsPrompt = "The cards that are showing are:\n"
    private val cardPrompt = "The card is: "
    private val questionPrompt = "Question:\n"

    private var _binding: FragmentTarotQuestionBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    companion object {
    }

    private lateinit var viewModel: TarotQuestionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                Log.i(TAG, "Question Text View Focused")
            } else {
                // show edit text entered text when it unfocused
                Log.i(TAG, "Question Text View Lost Focus")

                // hide soft keyboard when edit text lost focus
                hideSoftKeyboard(binding.QuestionTextView)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!AccountInformation.isLoggedIn) {
            Toast.makeText(context, "Please Login to Continue.", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                TarotQuestionFragmentDirections.actionToAccountFragment()
            )
        } else {
            /// getCoins
            val coinsText = "Coins: ${AccountInformation.coins.value}"
            binding.coinsTextView2.text = coinsText
            if (AccountInformation.coins.value < 1) {
                showCustomDialog(text = "You are out of coins. You will need to purchase more coins for more readings.")
                navigatePurchase()
            } else {


                if (viewModel.justLaunched) {
                    viewModel.justLaunched = false
                    showCustomDialog(text = getString(R.string.concentrate))
                } else {

                    if (listOf(10,25,50,75).contains(AccountInformation.ratingCount)) {
                        rateDialog()
                    }

                }

            }

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
            openAi.clearHistory()
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
            AccountInformation.ratingCount+=1
            lifecycleScope.launch {
                val response= openAi.askGPT(content, modelIdFortune)
                binding.progressBar.visibility = View.INVISIBLE
                if (response.status=="OK") {
                    AccountInformation.coins.value = AccountInformation.coins.value - 1
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
                dealButton.isEnabled = true
            }
            openAi.clearHistory()
            viewModel.gamePlay = GamePlay.NOTDEALT
            notDealt()
            showCardsFaceDown()


    }


    fun clickCardOne() {

        openAi.clearHistory()
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[0]
        if (card != null) showCard(card)

    }

    fun clickCardTwo() {

        openAi.clearHistory()
        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[1]
        if (card != null) showCard(card)
    }

    fun clickCardThree() {
        openAi.clearHistory()

        binding.QuestionTextView.clearFocus()
        val card = viewModel.hand[2]
        if (card != null) showCard(card)
    }

    fun showCardsFaceDown () {

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

    fun showCardsFaceUp () {

        binding.cardOneView.let {
            it.contentDescription = viewModel.hand[0]?.text
            setCardPicture(requireContext(), it, viewModel.hand[0]?.filename)
        }
        binding.cardTwoView.let {
            it.contentDescription = viewModel.hand[1]?.text
            setCardPicture(requireContext(), it, viewModel.hand[1]?.filename)
        }
        binding.cardThreeView.let {
            it.contentDescription = viewModel.hand[2]?.text
            setCardPicture(requireContext(), it, viewModel.hand[2]?.filename)
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
        AccountInformation.ratingCount+=1

        lifecycleScope.launch {
            val response = openAi.askGPT(cardPrompt + (card?.text ?: ""), modelIdCard)
            binding.progressBar.visibility = View.INVISIBLE
            if (response.status=="OK") {
                AccountInformation.coins.value = AccountInformation.coins.value -1
                findNavController().navigate(
                    TarotQuestionFragmentDirections.actionViewCard(response.message,
                        card?.filename ?: ""
                    )
                )
            } else {
                Toast.makeText(context,"Error:\n${response.message}",Toast.LENGTH_LONG).show()
                clickPlayAgain()
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
            TarotQuestionFragmentDirections.actionToPurchaseFragment())
    }


    private fun showCustomDialog(text: String) {
        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.question_dialog, null)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialogView)

        // Set the message and button click listener
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

        // Set the message and button click listener

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

    fun rateApp() {
        val appPackageName = "net.holybee.tarot"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

}



