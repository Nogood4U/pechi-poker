package com.pechi.poker.game

import com.pechi.poker.deck.PokerHandType


class HighCard : ShowdownHand {
    override fun analyze(player: Player, mGame: Game): PokerHand? = PokerHand(PokerHandType.CARTA_ALTA, 0)

    override fun getType(): PokerHandType = PokerHandType.CARTA_ALTA

    override fun untie(players: List<Player>, mGame: Game): Player? =
            players.map {
                it to listOf(it.cards, mGame.state.tableCards).flatten().reduce { acc, pokerCard ->
                    if (acc.number > pokerCard.number) acc else pokerCard
                }
            }.reduce { acc, pair ->
                if (pair.second.number > acc.second.number) pair else acc
            }.first
}

class Pair:ShowdownHand{
    override fun analyze(player: Player, mGame: Game): PokerHand? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): PokerHandType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun untie(players: List<Player>, mGame: Game): Player? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}