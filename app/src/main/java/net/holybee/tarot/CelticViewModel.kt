package net.holybee.tarot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.holybee.tarot.holybeeAPI.AccountInformation
private const val TAG = "CelticViewModel"
class CelticViewModel : ViewModel() {
    val celticReadings = MutableStateFlow( mutableListOf<CelticReading>())
    val openAi = OpenAI_wlh
    private val modelIdFirst = "card"
    private val modelId = "card"
    private val cardPrompt = "The next card is "
    private val deck: MutableList<Card> =  Card.values().toMutableList()
    private var deckIndex = 0
    val hand: MutableList<Card> = mutableListOf()
    var gamePlay = GamePlay.NOTDEALT
    var handSize = 10
    var justLaunched = true
    val coins = AccountInformation.coins

    fun shuffle() {

        deck.shuffle()
    }

    fun deal() {
        celticReadings.value.clear()

        if (deckIndex >= 74) {
            deckIndex = 0
        }
        if (deckIndex == 0) {
            shuffle()
        }
        for (i in 0..(handSize -1)) {
            hand[ i ] = deck[ deckIndex + i ]
            celticReadings.value.add(
                CelticReading(
                    card = hand[i],
                    result = "",
                    done = false
            )
            )
        }

        deckIndex += handSize
    }

    fun startReading() {

        viewModelScope.launch {
            celticReadings.value.forEach() {
                val response = openAi.askGPT(cardPrompt + it.card.text, modelIdFirst)
                if (response.status == "OK") {
                    it.result = response.message
                    it.done = true
                } else {
                    it.result = "Error with Reading. Please try again."
                    it.done = true
                }
            }
        }

    }
}