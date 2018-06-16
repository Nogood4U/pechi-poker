package com.pechi.poker.game

import com.pechi.poker.deck.PokerHandType
import com.pechi.poker.deck.PokerNumber
import kotlin.Pair


class HighCard : ShowdownHand {
    override fun getPriority(): Int = 0

    override fun analyze(player: Player, mGame: Game): PokerHand? = PokerHand(PokerHandType.CARTA_ALTA, getPriority())

    override fun getType(): PokerHandType = PokerHandType.CARTA_ALTA

    override fun untie(players: List<Player>, mGame: Game): Player? =
            players.map {
                println("HighCard Player ${it.name} hand ${listOf(it.cards, mGame.state.tableCards).flatten()}")
                it to listOf(it.cards, mGame.state.tableCards).flatten().reduce { acc, pokerCard ->
                    if (acc.number > pokerCard.number) acc else pokerCard
                }
            }.reduce { acc, pair ->
                if (pair.second.number > acc.second.number) pair else acc
            }.first
}

class OnePair : ShowdownHand {

    override fun getPriority(): Int = 1

    override fun analyze(player: Player, mGame: Game): PokerHand? {
        println("One Pair Player ${player.name} hand ${listOf(player.cards, mGame.state.tableCards).flatten()}")
        return if (listOf(player.cards, mGame.state.tableCards)
                        .flatten()
                        .groupBy { it.number }
                        .filter { it.value.size == 2 }
                        .isNotEmpty())
            PokerHand(PokerHandType.PAR, getPriority())
        else
            null
    }

    override fun getType(): PokerHandType = PokerHandType.PAR


    override fun untie(players: List<Player>, mGame: Game): Player? {
        println("Resolving OnePair Tie")
        val player = players.map {
            it to listOf(it.cards, mGame.state.tableCards)
                    .flatten()
                    .groupBy { it.number }
                    .filter { it.value.size == 2 }
                    .map { it.key }
                    .first()
        }
        val groupBy= player.groupBy { it.second }.toSortedMap(compareByDescending { it.ordinal })

        val res: Pair<Player, PokerNumber>? = if (groupBy[groupBy.firstKey()].orEmpty().size == 2) {
            when {
                player[0].second > player[1].second -> player[0]
                player[0].second == player[1].second -> null
                else -> player[1]
            }
        } else null

        val first: Pair<Player, PokerNumber>  by lazy {
            players.map {
                it to listOf(it.cards, mGame.state.tableCards)
                        .flatten()
                        .groupBy { it.number }
                        .filter { it.value.size == 1 }
                        .map { it.key }
                        .first()
            }.sortedByDescending { it.second }.first()
        }

        return res?.first ?: first.first
    }

}