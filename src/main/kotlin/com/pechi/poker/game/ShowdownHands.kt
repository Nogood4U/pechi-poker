package com.pechi.poker.game

import com.pechi.poker.deck.PokerHandType


class HighCard:ShowdownHand{
    override fun analyze(player: Player): PokerHand? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): PokerHandType = PokerHandType.CARTA_ALTA

    override fun untie(players: List<Player>): Player? = null

}