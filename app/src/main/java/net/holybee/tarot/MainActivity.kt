package net.holybee.tarot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.holybee.tarot.holybeeAPI.AccountInformation


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AccountInformation.readAuthToken(application)

    }



}

