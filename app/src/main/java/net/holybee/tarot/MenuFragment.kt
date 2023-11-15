package net.holybee.tarot

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import net.holybee.tarot.databinding.FragmentMenuBinding
import net.holybee.tarot.databinding.FragmentTarotQuestionBinding
import net.holybee.tarot.holybeeAPI.AccountInformation


class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuRate.setOnClickListener { clickRate() }
        binding.menu3cardReading.setOnClickListener { click3card() }
        binding.menuCelticReading.setOnClickListener { clickCeltic() }
        binding.menuPurchase.setOnClickListener { clickPurchase() }
        binding.menuAccount.setOnClickListener { clickAccount() }
    }

    companion object {

    }

    override fun onResume() {
        super.onResume()
        if (!AccountInformation.isLoggedIn) {
            Toast.makeText(context, "Please Login to Continue.", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                MenuFragmentDirections.actionMenuFragmentToAccountFragment()
            )
        } else {

            if ((AccountInformation.coins.value?.compareTo(0) ?: 0) < 1) {
                showCustomDialog(text = "You are out of coins. You will need to purchase more coins for more readings.")
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
        findNavController().navigate(
            MenuFragmentDirections.actionMenuFragmentToCelticFragment()
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


    fun clickRate() {
        val appPackageName = "net.holybee.tarot"
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
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
}