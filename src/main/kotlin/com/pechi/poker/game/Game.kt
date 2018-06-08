package com.pechi.poker.game

import com.pechi.poker.deck.PokerCard
import com.pechi.poker.deck.PokerDeck
import com.pechi.poker.deck.PokerHand

data class Game(val deck: PokerDeck, val players: List<Player>, var state: State, private var moves: List<Move>) {

    fun drawCard(): Unit {
        val card = deck.mdeck.pop()
        val move = Move(card)
        addMove(move)
        applyMove(move)
    }

    fun dropCard(): PokerCard {
        val card = deck.mdeck.pop()
        addMove(Move(card, MoveType.BOTAR))
        return card
    }

    fun addMove(move: Move): Unit {
        this.moves += move
    }

    fun applyMove(move: Move): Unit {
        state = this.state.apply(move)
    }
}


enum class MoveType {
    BOTAR, JUGAR
}

data class Move(val card: PokerCard, val type: MoveType = MoveType.JUGAR)


class Player(cards: List<PokerCard>, hand: PokerHand)


data class State(private val deck: PokerDeck, private val tableCards: List<PokerCard>) {

    fun apply(move: Move): State {
        this.deck.removeCard(move.card)

        return State(this.deck, tableCards + move.card)
    }

    companion object {
        fun newState(deck: PokerDeck) = State(deck, emptyList())
    }
}


class GameMatch(players: List<Player>) {

    val MAX_CARDS_TABLE = 5
    val START_CARDS_TABLE = 3
    val mDeck = PokerDeck()
    val mGame: Game = Game(mDeck, players, State.newState(mDeck), emptyList())
    var droppedCard: List<PokerCard> = emptyList()

    fun start() {
        droppedCard += mGame.dropCard()
        mGame.drawCard()
        mGame.drawCard()
        mGame.drawCard()
    }

    fun deal(): Unit {
        mGame.drawCard()
    }
}