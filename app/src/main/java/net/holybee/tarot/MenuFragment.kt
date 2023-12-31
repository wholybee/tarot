package net.holybee.tarot

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import net.holybee.tarot.databinding.FragmentMenuBinding
import net.holybee.tarot.holybeeAPI.AccountInformation


class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val  coinsObserver = { coins:Int ->
        val coinsText = "Coins: $coins"
        binding.coinsTextView.text = coinsText
    }

    private val openAi = OpenAI_wlh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MENU","onCreate")

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuRate.setOnClickListener { Dialogs.rateApp(this) }
        binding.menu3cardReading.setOnClickListener { click3card() }
        binding.menuCelticReading.setOnClickListener { clickCeltic() }
        binding.menuHoroscope.setOnClickListener { clickHoroscope() }
        binding.menuPurchase.setOnClickListener { clickPurchase() }
        binding.menuAccount.setOnClickListener { clickAccount() }
    }

    companion object {

    }

    override fun onResume() {
        Log.i("MENU","onResume")
        super.onResume()
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(false)
        myActionbar?.hide()
        AccountInformation.coins.observe(viewLifecycleOwner, coinsObserver)

        if (!AccountInformation.isLoggedIn) {
            Toast.makeText(context, "Please Login to Continue.", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                MenuFragmentDirections.actionMenuFragmentToAccountFragment()
            )
        } else {

            if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
                Dialogs.showCustomDialog(requireActivity(), layoutInflater, "You are out of coins. You will need to purchase more coins for more readings.")
                clickPurchase()
            }
        }
    }

    private fun click3card() {
        findNavController().navigate(
            MenuFragmentDirections.actionMenuFragmentToTarotReading()
        )
    }

    private fun clickCeltic() {
        openAi.clearHistory()
        findNavController().navigate(
            MenuFragmentDirections.actionMenuFragmentToCelticFragment()
        )

    }

    private fun clickHoroscope() {
        openAi.clearHistory()
        findNavController().navigate(
            MenuFragmentDirections.actionToHoroscopeFragment()
        )

    }

    private fun clickPurchase() {
        findNavController().navigate(
            MenuFragmentDirections.actionMenuFragmentToPurchaseFragment()
        )

    }

    private fun clickAccount() {
        findNavController().navigate(
            MenuFragmentDirections.actionMenuFragmentToAccountFragment()
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        AccountInformation.coins.removeObserver(coinsObserver)
    }
}