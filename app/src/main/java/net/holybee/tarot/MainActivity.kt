package net.holybee.tarot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.facebook.appevents.AppEventsLogger
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.GetCoinsResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient

private const val TAG = "Main Activity"
class MainActivity : AppCompatActivity(), GetCoinsResponseListener {

    lateinit var logger:AppEventsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger  = AppEventsLogger.newLogger(applicationContext)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.hide()

        AccountInformation.readLoginInfo(application)

        if (AccountInformation.isLoggedIn) {
            val client = HolybeeAPIClient
            client.getCoins(this)
        }

        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val dpHeight = displayMetrics.heightPixels / displayMetrics.density
        Log.e(TAG,"dp Height: $dpHeight  dp Width: $dpWidth")

    }
    override fun onStop() {
        super.onStop()
        AccountInformation.save(application)
    }

    override fun onGetCoinSuccess(coins: Int) {

        AccountInformation.coins.postValue(coins)
        val coinText = "Coins: ${AccountInformation.coins}"
        Log.i(TAG, coinText)
    }

    override fun onGetCoinsFail(result: String) {
        Log.e(TAG, "getCoins Failed $result")
    }

}

