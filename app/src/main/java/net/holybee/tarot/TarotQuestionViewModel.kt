package net.holybee.tarot

import android.util.Log
import androidx.lifecycle.ViewModel
private const val TAG = "TarotQuestionViewModel"

class TarotQuestionViewModel : ViewModel() {
    private val deck: MutableList<Card> =  Card.values().toMutableList()
    private var deckIndex = 0
    val hand: MutableList<Card?> = mutableListOf(null,null,null)
    var gamePlay = GamePlay.NOTDEALT
    var handSize = 3
    var justLaunched = true

    fun shuffle() {
        Log.d (TAG,"shuffle")
        deck.shuffle()
    }

    fun deal() {
        Log.d(TAG,"deckIndex = $deckIndex")
        if (deckIndex >= 74) {
            deckIndex = 0
        }
        if (deckIndex == 0) {
            shuffle()
        }
        for (i in 0..(handSize -1)) {
            hand[ i ] = deck[ deckIndex + i ]
        }

        deckIndex = deckIndex + 3
    }

}