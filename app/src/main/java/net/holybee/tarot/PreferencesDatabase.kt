package net.holybee.tarot



import android.icu.text.DateFormat
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Update
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date


private const val TAG="PreferencesDB"
private val dateFormat = DateFormat.getDateTimeInstance()

@Entity
data class Pref(
    @PrimaryKey
    @ColumnInfo(name = "pref") val pref: String,
    @ColumnInfo(name = "value") val value: String
)

@Dao
interface PrefsDao {
    @Query("SELECT * FROM pref")
    suspend fun getAll(): List<Pref>

    @Query("SELECT * FROM pref WHERE pref LIKE :key LIMIT 1" )
    suspend fun getByKey(key: String): Pref

    @Insert
    suspend fun insertPref(pref: Pref)

    @Update
    suspend fun updatePref(pref: Pref)

    @Insert
    suspend fun insertAll(vararg pref: Pref)

    @Query("DELETE FROM pref WHERE pref LIKE :key ")
    suspend fun delete(key: String)

    @Query("DELETE FROM pref")
    suspend fun deleteAll()
}

@Database(entities = [Pref::class], version = 1)
abstract class PrefsDatabase : RoomDatabase() {
    abstract fun prefsDao(): PrefsDao
}



class PreferencesDb private constructor()  {
    private val applicationContext = TarotApplication.applicationContext()
    private val applicationScope = CoroutineScope(SupervisorJob())


    private val db = Room.databaseBuilder(
        applicationContext,
        PrefsDatabase::class.java, "preferences_database"
    ).build()

    val prefsDao = db.prefsDao()

    companion object {
        @Volatile
        private var instance: PreferencesDb? = null
        private var prefsDao: PrefsDao? = null

         var token: String? = null
            set(value) {
                field = value
                CoroutineScope(SupervisorJob()).launch {
                    prefsDao?.updatePref(
                        Pref("token",value?:"")
                    )
                }
            }

        fun getInstance(): PreferencesDb {
            Log.d(TAG,"getInstance")
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = PreferencesDb()
                        prefsDao = instance!!.prefsDao
                        instance!!.populateDb()
                    }
                }
            }
            return instance!!
        }

        fun getPrefsDao():PrefsDao {
            Log.d(TAG,"getPrefsDao")
            return getInstance().prefsDao
        }
        val ONE_MINUTE_IN_MILLIS: Long = 60000
        fun addMinutesToDate(minutes: Int, beforeTime: Date): Date {
            val curTimeInMs = beforeTime.time
            return Date(
                curTimeInMs + minutes * ONE_MINUTE_IN_MILLIS
            )
        }

    }

    suspend fun updateLocalFromDb() {

                token = prefsDao.getByKey("token").value

        Log.d(TAG,"end updateLocalFromDb")

    }

    private fun populateDb() {

        applicationScope.launch {

            val dbToken = prefsDao.getByKey("token")
            if (dbToken == null) {
                token = ""
                prefsDao.insertPref(Pref(pref="token",value= token!!))
            }

            Log.d(TAG,"end populateDb")
            updateLocalFromDb()

        }

    }
}