package net.holybee.tarot

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class TarotApplication : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: TarotApplication? = null


    }

}


