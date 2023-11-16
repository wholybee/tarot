package net.holybee.tarot

import androidx.lifecycle.ViewModel

private const val TAG = "CelticViewModel"
class CelticViewModel : ViewModel() {


    private val deck: MutableList<Card> =  Card.values().toMutableList()

    private var deckIndex = 0
    val hand: MutableList<Card> = mutableListOf()

    var gamePlay = GamePlay.NOTDEALT
    var handSize = 10
    var justLaunched = true


    fun shuffle() {

        deck.shuffle()
    }

    fun deal() {

        hand.clear()
        if (deckIndex >= 74) {
            deckIndex = 0
        }
        if (deckIndex == 0) {
            shuffle()
        }
        for (i in 0..(handSize -1)) {
            hand.add(deck[ deckIndex + i ])
        }

        deckIndex += handSize
    }
}