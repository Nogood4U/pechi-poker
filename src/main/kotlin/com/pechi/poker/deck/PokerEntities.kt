package com.pechi.poker.deck

import java.util.*


enum class PokerSuits {
    PICA, CORAZON, TREBOL, DIAMANTE
}

enum class PokerNumber {
    DOS, TRES, CUATRO, CINCO, SEIS, SIETE, OCHO, NUEVE, DIEZ, J, K, Q, A
}

enum class PokerHandType {
    CARTA_ALTA, PAR, DOBLE_PAR, TRIO, ESCALERA, COLOR, FULL, POKER, ESCALERA_COLOR, ROYAL
}

class PokerDeck {

    var mdeck: Deque<PokerCard> = init()

    fun init(): Deque<PokerCard> {
        var deck1 = generateDeck()
        deck1 = shuffleDeck(12, deck1)
        return deck1
    }

    private fun shuffleDeck(times: Int, deck: Deque<PokerCard>): Deque<PokerCard> {
        var _deck = deck
        repeat(times) {
            _deck = _deck.toList().shuffled().toCollection(LinkedList())
        }
        return _deck
    }

    fun removeCard(card: PokerCard): Unit {
        mdeck = mdeck.filter {
            it != card
        }.toCollection(LinkedList())
    }

    private fun generateDeck(): Deque<PokerCard> {
        return PokerNumber.values().flatMap { num ->
            PokerSuits.values().map { suit ->
                PokerCard(suit, num)
            }
        }.toCollection(LinkedList())

    }

}

data class PokerCard(val suits: PokerSuits, val number: PokerNumber){
    override fun toString(): String {
        return "[$number de $suits]"
    }
}
