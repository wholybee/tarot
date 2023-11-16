package net.holybee.tarot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.holybee.tarot.holybeeAPI.AccountInformation
private const val TAG = "CelticViewModel"
class CelticViewModel : ViewModel() {
    val _celticReadings : MutableList<MutableLiveData<CelticReading>> = mutableListOf() // MutableLiveData( mutableListOf<CelticReading>())
    val celticReadings: List<LiveData<CelticReading>> = _celticReadings // LiveData<MutableList<CelticReading>> = _celticReadings
    val openAi = OpenAI_wlh
    private val modelIdFirst = "celtic1stcard"
    private val modelId = "celtic"
    private val cardPrompt = "Provide an Interpretation of Card "
    private val cardPrompt1: String
        get() {
            val cards = hand.withIndex().joinToString("\n") { (index, card) ->
                "Card ${index+1}: ${card.text}"
            }
            return "$cards\n${cardPrompt}1: ${hand[0].text}\n"
        }

    private val deck: MutableList<Card> =  Card.values().toMutableList()
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
        "Outcome")

    private var deckIndex = 0
    val hand: MutableList<Card> = mutableListOf()
    var gamePlay = GamePlay.NOTDEALT
    var handSize = 10
    var justLaunched = true
    var index = 0

    fun shuffle() {

        deck.shuffle()
    }

    fun deal() {
        _celticReadings.clear()
        hand.clear()
        if (deckIndex >= 74) {
            deckIndex = 0
        }
        if (deckIndex == 0) {
            shuffle()
        }
        for (i in 0..(handSize -1)) {
            hand.add(deck[ deckIndex + i ])
            _celticReadings.add(
                MutableLiveData(
                    CelticReading(
                        card = hand[i],
                        result = "",
                        done = false
                    )
                )
            )
        }

        deckIndex += handSize
    }



    fun startReading() {

        viewModelScope.launch {
            Log.i(TAG,"Start Reading")
            _celticReadings.forEachIndexed { index, it ->
                Log.i(TAG,"Reading " + it.value!!.card.text)
                var prompt = ""
                if (index == 0) {
                    prompt = cardPrompt1
                } else {
                    prompt = "$cardPrompt${index+1}: ${it.value!!.card.text}"
                }
                System.out.println(prompt)

                AccountInformation.ratingCount += 1
                AccountInformation.coins.postValue(AccountInformation.coins.value?.minus(1))
                val response = openAi.askGPT(prompt, modelId)
                if (response.status == "OK") {
                    val newCard = CelticReading (
                        it.value!!.card,
                        response.message,
                        true
                    )
                    it.value = newCard

                } else {
                    val newCard = CelticReading (
                        it.value!!.card,
                        "Error with Reading. Please try again.",
                        true
                    )
                    it.value=newCard

                }
            }
        }

    }
}