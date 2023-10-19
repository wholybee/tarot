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


        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
        fun applicationScope() : CoroutineScope {
            return CoroutineScope(SupervisorJob())
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

}


