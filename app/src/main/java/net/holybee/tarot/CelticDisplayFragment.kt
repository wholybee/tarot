package net.holybee.tarot

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import net.holybee.tarot.databinding.FragmentCelticBinding
import net.holybee.tarot.databinding.FragmentCelticDisplayBinding
import net.holybee.tarot.holybeeAPI.AccountInformation

private const val TAG = "CelticDisplayFragment"

class CelticDisplayFragment : Fragment() {

    private var _binding: FragmentCelticDisplayBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val  coinsObserver = { coins:Int ->
        val coinsText = "Coins: $coins"
        binding.coinsTextView.text = coinsText
    }
    companion object {
        fun newInstance() = CelticDisplayFragment()
    }

    lateinit var viewModel: CelticViewModel

    val readingObserver = Observer<CelticReading> { celticReadings ->
        Log.e(TAG,"observe reading")
        if (celticReadings.done == false) {
            binding.progressBar.visibility = View.VISIBLE
            binding.nextButton.isEnabled = false
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.nextButton.isEnabled = true
        }
        binding.cardDescriptionTextView.setText(
            celticReadings.result
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG,"onCreateView")
        _binding = FragmentCelticDisplayBinding.inflate(inflater, container, false)

        binding.cardDescriptionTextView.movementMethod= ScrollingMovementMethod()
        binding.nextButton.setOnClickListener { clickButton() }
        binding.prevButton.setOnClickListener { clickPrevious() }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CelticViewModel::class.java)

        AccountInformation.coins.observe(viewLifecycleOwner, coinsObserver)

        viewModel.index = 0
        if (viewModel.gamePlay != GamePlay.ASKED) {
            AccountInformation.coins.postValue(AccountInformation.coins.value?.minus(1) ?: 0)
            viewModel.startReading()
            viewModel.gamePlay = GamePlay.ASKED
        }
        if (viewModel.index == 0) binding.prevButton.visibility = View.INVISIBLE
        if (viewModel.index == 9) {
            binding.nextButton.text = "Done"
        } else {
            binding.nextButton.text = "Next"
        }
        if (viewModel.index > 9 || viewModel.index < 0) viewModel.index =0
        displayCard(viewModel.index)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.back_only, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {


            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }
            R.id.navigate_back ->{
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    private fun displayCard (index: Int) {
        if (index >= viewModel.celticReadings.size ) {
            Log.e(TAG,"Error! Index is out of bounds:"+index.toString())
            return
        }

        if (viewModel.celticReadings[index].value == null  ) {
            Log.e(TAG, "Error! Celtic Reading value is null!")
            return
        }
        Log.i(TAG,"display card " + index.toString())
        Log.i(TAG,"Hand size " + viewModel.hand.size.toString() )
        Log.i(TAG, viewModel.celticReadings[index].value!!.card.filename)

        setCardPicture(requireContext(), binding.cardView,
            viewModel.celticReadings[index].value!!.card.filename
        )
        viewModel.celticReadings[index].observe(viewLifecycleOwner, readingObserver)

    }

    private fun removeObserver(index: Int) {
        if (index in 0..9)
        {
            viewModel.celticReadings[index].removeObserver(readingObserver)
        }
    }

    private fun clickPrevious () {
        if (viewModel.index > 0) {
            removeObserver(viewModel.index)
            viewModel.index --
            binding.nextButton.text = "Next"
            binding.nextButton.tag = ""
        }
        if (viewModel.index == 0) binding.prevButton.visibility = View.INVISIBLE
        displayCard(viewModel.index)

    }

    private fun clickButton() {


        if (binding.nextButton.tag == "done") {
            binding.nextButton.tag = ""
            binding.nextButton.text = "Next"
            Log.i(TAG,"Clicked done, leaving")
            findNavController().navigateUp()
            return
        }
        if (viewModel.index in 0..9) removeObserver(viewModel.index)
        viewModel.index+=1
        displayCard(viewModel.index)

        if (viewModel.index == 9) {
            Log.i(TAG,"index at 9")
            binding.nextButton.text = "Done"
            binding.nextButton.tag = "done"

        } else {
            binding.nextButton.text = "Next"
            binding.nextButton.tag = ""
        }
        if (viewModel.index == 0) {
            Log.i(TAG,"index at 0")
            binding.prevButton.visibility = View.INVISIBLE
        }   else {
            binding.prevButton.visibility = View.VISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        AccountInformation.coins.removeObserver(coinsObserver)
    }

}