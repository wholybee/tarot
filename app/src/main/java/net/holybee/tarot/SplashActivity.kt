package net.holybee.tarot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient
import net.holybee.tarot.holybeeAPI.LoginResponseListener

private const val TAG = "Splash Activity"

class SplashActivity : AppCompatActivity(), LoginResponseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Simulate setup tasks with a Handler and delay
        val handler = android.os.Handler()
        handler.postDelayed({

            // Perform your setup tasks here

            // Load Preferences
            AccountInformation.readLoginInfo(application)
            Log.d(
                TAG,
                "Username: ${AccountInformation.username}\nPassword: ${AccountInformation.password}"
            )
            // If username and password
            if (AccountInformation.username != "" && AccountInformation.password != "") {
                // attempt login
                val client = HolybeeAPIClient
                client.loginAsync(AccountInformation.username, AccountInformation.password, this)
            } else {

                // Once setup tasks are complete, navigate to the main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 2000) // Delay for 2 seconds (adjust as needed)
    }

    override fun onLoginSuccess(userToken: String, coins: Int) {
        // Once setup tasks are complete, navigate to the main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onLoginFail(result: String) {
        // Once setup tasks are complete, navigate to the main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}