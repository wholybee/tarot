package net.holybee.tarot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.GetCoinsResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient

private const val TAG = "Main Actvity"
class MainActivity : AppCompatActivity(), GetCoinsResponseListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        AccountInformation.readLoginInfo(application)

        if (AccountInformation.isLoggedIn) {
            val client = HolybeeAPIClient
            client.getCoins(this)
        }

    }
    override fun onStop() {
        super.onStop()
        AccountInformation.save(application)
    }

    override fun onGetCoinSuccess(coins: Int) {
        Log.d(TAG, coins.toString())
        AccountInformation.coins = coins
        val coinText = "Coins: ${AccountInformation.coins}"
        Log.d(TAG, coinText)
    }

    override fun onGetCoinsFail(result: String) {
        Log.d(TAG, "getCoins Failed $result")
    }
}

