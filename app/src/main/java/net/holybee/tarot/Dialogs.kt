package net.holybee.tarot

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import net.holybee.tarot.holybeeAPI.AccountInformation

object Dialogs {

    fun showCustomDialog(activity: Context, layoutInflater: LayoutInflater, text: String) {
        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.question_dialog, null)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(activity)
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

    fun rateDialog(activity: Context, layoutInflater: LayoutInflater, fragment: Fragment) {
        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rate_dialog, null)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)

        // Set the message and nextButton click listener

        val acceptButton = dialogView.findViewById<Button>(R.id.accept_button)
        val declineButton = dialogView.findViewById<Button>(R.id.decline_button)

        val dialog = builder.create()

        acceptButton.setOnClickListener {
            AccountInformation.hasRated= true
            dialog.dismiss()
            rateApp(fragment)
        }
        declineButton.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }

    fun rateApp(fragment: Fragment) {
        AccountInformation.hasRated = true
        val appPackageName = "net.holybee.tarot"
        try {
            fragment.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            fragment.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

}