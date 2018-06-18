package com.pechi.poker.game

import com.pechi.poker.deck.PokerHandType
import com.pechi.poker.deck.PokerNumber
import kotlin.Pair


class HighCard : ShowdownHand {
    override fun getPriority(): Int = 0

    override fun analyze(player: Player, mGame: Game): PokerHand? = PokerHand(PokerHandType.CARTA_ALTA, getPriority())

    override fun getType(): PokerHandType = PokerHandType.CARTA_ALTA

    override fun untie(players: List<Player>, mGame: Game): Player? {
        return ShowdownUtils.playerWithHighestCard(players, mGame)
    }

}

class OnePair : ShowdownHand {

    override fun getPriority(): Int = 1

    override fun analyze(player: Player, mGame: Game): PokerHand? {
        return if (listOf(player.cards, mGame.state.tableCards)
                        .flatten()
                        .groupBy { it.number }
                        .filter { it.value.size == 2 }
                        .isNotEmpty()) {
            player.hand = PokerHand(PokerHandType.PAR, getPriority())
            player.hand
        } else
            null
    }

    override fun getType(): PokerHandType = PokerHandType.PAR


    override fun untie(players: List<Player>, mGame: Game): Player? {
        println("Resolving OnePair Tie")
        var player = players.map {
            it to listOf(it.cards, mGame.state.tableCards)
                    .flatten()
                    .groupBy { it.number }
                    .filter { it.value.size == 2 }
                    .map { it.key }
                    .first()
        }
        val groupBy = player.groupBy { it.second }.toSortedMap(compareByDescending { it.ordinal })

        val res: Pair<Player, PokerNumber>? = if (groupBy[groupBy.firstKey()].orEmpty().size > 1) {
            player = groupBy[groupBy.firstKey()].orEmpty()
            null
        } else groupBy[groupBy.firstKey()].orEmpty().first()

        val first by lazy {
            val map: List<Player> = player.map { it.first }
            ShowdownUtils.playerWithHighestCard(map, mGame)
        }

        return res?.first ?: first
    }

}

class TwoPair : ShowdownHand {
    override fun analyze(player: Player, mGame: Game): PokerHand? {
        val groupedCards = listOf(player.cards, mGame.state.tableCards)
                .flatten()
                .groupBy { it.number }
                .filter { it.value.size == 2 }

        return if (groupedCards.isNotEmpty() && groupedCards.keys.size == 2) {
            player.hand = PokerHand(PokerHandType.DOBLE_PAR, getPriority())
            player.hand
        } else
            null
    }

    override fun getType(): PokerHandType = PokerHandType.DOBLE_PAR


    override fun getPriority(): Int = 3


    override fun untie(players: List<Player>, mGame: Game): Player? {
        var player = players.map {
            it to listOf(it.cards, mGame.state.tableCards)
                    .flatten()
                    .groupBy { it.number }
                    .filter { it.value.size == 2 }
                    .map { it.key }
                    .sumBy { it.ordinal }
        }
        val groupBy = player.groupBy { it.second }.toSortedMap(compareByDescending { it })
        val res = if (groupBy[groupBy.firstKey()].orEmpty().size > 1) {

            player = groupBy[groupBy.firstKey()].orEmpty()
            null

        } else groupBy[groupBy.firstKey()].orEmpty().first().first

        val first by lazy {
            val map: List<Player> = player.map { it.first }
            ShowdownUtils.playerWithHighestCard(map, mGame)
        }

        return res ?: first
    }

}

class ThreeOfaKind : ShowdownHand {
    override fun analyze(player: Player, mGame: Game): PokerHand? {
        return if (listOf(player.cards, mGame.state.tableCards)
                        .flatten()
                        .groupBy { it.number }
                        .filter { it.value.size == 3 }
                        .isNotEmpty()) {
            player.hand = PokerHand(PokerHandType.TRIO, getPriority())
            player.hand
        } else
            null
    }

    override fun getType(): PokerHandType {
        return PokerHandType.TRIO
    }

    override fun getPriority(): Int = 4


    override fun untie(players: List<Player>, mGame: Game): Player? {
        println("Resolving ThreeOfaKind Tie")
        var player = players.map {
            it to listOf(it.cards, mGame.state.tableCards)
                    .flatten()
                    .groupBy { it.number }
                    .filter { it.value.size == 3 }
                    .map { it.key }
                    .first()
        }
        val groupBy = player.groupBy { it.second }.toSortedMap(compareByDescending { it.ordinal })

        val res: Pair<Player, PokerNumber>? = if (groupBy[groupBy.firstKey()].orEmpty().size > 1) {
            player = groupBy[groupBy.firstKey()].orEmpty()
            null
        } else groupBy[groupBy.firstKey()].orEmpty().first()

        val first by lazy {
            val map: List<Player> = player.map { it.first }
            ShowdownUtils.playerWithHighestCard(map, mGame)
        }

        return res?.first ?: first
    }

}

object ShowdownUtils {

    fun playerWithHighestCard(players: List<Player>, mGame: Game): Player? {
        val reduce = players.map {
            it to listOf(it.cards).flatten().sortedByDescending { it.number }
        }

        for (i in reduce.first().second.indices) {
            val fold = reduce.map { it.first to it.second[i].number }
                    .fold(null as Pair<Player, PokerNumber>?) { a, b ->
                        if (a != null) {
                            when {
                                a.second > b.second -> a
                                a.second < b.second -> b
                                else -> null
                            }
                        } else b
                    }
            if (fold != null) {
                return fold.first
            }
        }
        return null
    }
}