package net.holybee.tarot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.holybee.tarot.holybeeAPI.AccountInformation
private const val TAG="CelticDisplayViewModel"
class CelticDisplayViewModel : ViewModel() {
    val _celticReadings: MutableList<MutableLiveData<CelticReading>> = mutableListOf()
    val celticReadings: List<LiveData<CelticReading>> = _celticReadings
    val openAi = OpenAI_wlh
    private val modelIdFirst = "celtic1stcard"
    private val modelId = "celtic"
    private val cardPrompt = "Provide an Interpretation of Card "
    private val cardPrompt1: String
        get() {
            val cards = celticReadings.withIndex().joinToString("\n") { (index, celticReading) ->
                "Card ${index + 1}: ${celticReading.value?.card?.text}"
            }
            return "$cards\n${cardPrompt}1: ${celticReadings[0].value?.card?.text}\n"
        }

    private val deck: MutableList<Card> = Card.values().toMutableList()
    val positions = listOf<String>(
        "The Present",
        "The Challenge",
        "The Past",
        "The Future",
        "Above",
        "Below",
        "Advice",
        "External Influences",
        "Hopes or Fears",
        "Outcome"
    )

    var index = 0


    fun startReading() {

        viewModelScope.launch {
            Log.i(TAG, "Start Reading")
            _celticReadings.forEachIndexed { index, it ->
                Log.i(TAG, "Reading " + it.value!!.card.text)
                var prompt = ""

                if (!it.value!!.done) {

                    if (index == 0) {
                        prompt = cardPrompt1
                    } else {
                        prompt = "$cardPrompt${index + 1}: ${it.value!!.card.text}"
                    }
                    System.out.println(prompt)

                    AccountInformation.ratingCount += 1
                    AccountInformation.coins.postValue(AccountInformation.coins.value?.minus(1))
                    val response = openAi.askGPT(prompt, modelId)
                    if (response.status == "OK") {
                        val newCard = CelticReading(
                            it.value!!.card,
                            response.message,
                            true
                        )
                        it.value = newCard

                    } else {
                        val newCard = CelticReading(
                            it.value!!.card,
                            "Error with Reading. Please try again.",
                            true
                        )
                        it.value = newCard

                    }
                }
            }
        }

    }

    fun populateCelticReadings(cardArray: Array<Card>) {
        Log.e(TAG, "populate")

        cardArray.forEachIndexed { index, card ->
            Log.e(TAG, "Card: ${card.text}")
            Log.i(TAG,"Celtic size ${_celticReadings.size} index $index")
            if (_celticReadings.size <= index) {
                Log.i(TAG,"Creating new card")
                _celticReadings.add(
                    MutableLiveData(
                        CelticReading(
                            card,
                            "",
                            false
                        )
                    )
                )
            } else if ((_celticReadings[index].value?.card?.text ?: "") != card.text)
                {
                    Log.i(TAG,"Replacing existing card")
                    _celticReadings[index] = (
                            MutableLiveData(
                                CelticReading(
                                    card,
                                    "",
                                    false
                                )
                            )
                            )

            } else
            {
                Log.i(TAG,"not changing card")
                Log.i(TAG,"name:${_celticReadings[index].value?.card?.text}")
                Log.i(TAG,"result:${_celticReadings[index].value?.result}")
                Log.i(TAG,"done:${_celticReadings[index].value?.done}")
            }
        }

    }
}