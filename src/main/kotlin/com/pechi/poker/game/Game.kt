package com.pechi.poker.game

import com.pechi.poker.deck.PokerCard
import com.pechi.poker.deck.PokerDeck
import com.pechi.poker.deck.PokerHandType
import java.util.*

data class Game(val deck: PokerDeck, val players: List<Player>, var state: State, private var moves: List<Move>) {

    fun drawCard() {
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

    fun addMove(move: Move) {
        this.moves += move
    }

    fun applyMove(move: Move) {
        state = this.state.apply(move)
    }

    fun init() {
        deck.init()
        (1..2).forEach {
            players.forEach {
                it.cards += deck.mdeck.pop()
            }
        }
    }
}

enum class MoveType {
    BOTAR, JUGAR, APOSTAR
}

data class Bet(val coin100: Int = 0, val coin50: Int = 0, val coin25: Int = 0, val coin10: Int = 0) {
    fun totalMoney(): Int {
        return (coin100 * 100) + (coin50 * 50) + (coin25 * 25) + (coin10 * 10)
    }
}

data class Move(val card: PokerCard, val type: MoveType = MoveType.JUGAR)


data class Player(val name: String, var cards: List<PokerCard> = emptyList(), var hand: PokerHand = PokerHand(PokerHandType.CARTA_ALTA, 0), var folded: Boolean = false, val coin100: Int = 10, val coin50: Int = 10, val coin25: Int = 10, val coin10: Int = 10) {

    fun totalMoney(): Int {
        return coin100 + coin50 + coin25 + coin10
    }

    fun placeBet(amount: Int): Bet {
        wants = listOf(
                Chip("100", 100, coin100),
                Chip("50", 50, coin50),
                Chip("25", 25, coin25),
                Chip("10", 10, coin10)
        )
        val (coins, _, _) = dist(wants.size - 1, amount)

        val groupedCoin = coins.groupBy(Chip::name)
        return Bet(groupedCoin.getOrElse("100") { emptyList() }.size,
                groupedCoin.getOrElse("50") { emptyList() }.size,
                groupedCoin.getOrElse("25") { emptyList() }.size,
                groupedCoin.getOrElse("10") { emptyList() }.size)
    }

    data class Chip(val name: String, val weight: Int, val value: Int)

    var wants = listOf(
            Chip("100", 100, 100),
            Chip("50", 50, 50),
            Chip("25", 25, 25),
            Chip("10", 10, 10)
    )


    fun dist(i: Int, w: Int): Triple<MutableList<Chip>, Int, Int> {
        val chosen = mutableListOf<Chip>()
        if (i < 0 || w == 0) return Triple(chosen, 0, 0)
        else if (wants[i].weight > w) return dist(i - 1, w)
        val (l0, w0, v0) = dist(i - 1, w)
        var (l1, w1, v1) = dist(i, w - wants[i].weight)
        v1 += wants[i].value
        if (v1 > v0) {
            l1.add(wants[i])
            return Triple(l1, w1 + wants[i].weight, v1)
        }
        return Triple(l0, w0, v0)
    }
}


data class State(val deck: PokerDeck, val tableCards: List<PokerCard>) {

    fun apply(move: Move): State {
        this.deck.removeCard(move.card)

        return State(this.deck, tableCards + move.card)
    }

    companion object {
        fun newState(deck: PokerDeck) = State(deck, emptyList())
    }
}


data class GameMatch(var players: List<Player>) {
    enum class GAME_STAGE {
        START_BETS, WAIT_FOR_BETS, DEALING, BLINDS, CALL_RAISE_FOLD, HIGHS, LOWS
    }

    val MAX_RAISE_NUM = players.size
    val MAX_CARDS_TABLE = 5
    val START_CARDS_TABLE = 3
    var minStartBetAmount = 20
    var minCurrentBetAmount = minStartBetAmount
    var currentRaises = 0
    val mDeck = PokerDeck()
    val mGame: Game = Game(mDeck, players, State.newState(mDeck), emptyList())
    var droppedCard: List<PokerCard> = emptyList()
    var high: Player = Player("")
    var low: Player = Player("")
    var game_stage = GAME_STAGE.WAIT_FOR_BETS
    var bets: MutableMap<Player, MutableList<Bet>> = HashMap()
    var totalBetToCall: Int = minStartBetAmount
    var turnPlayer: Int = 0


    fun nextTurn(): Player {
        return this.players[this.turnPlayer]
    }

    fun join(player: Player) {
        players += player
    }

    fun start() {
        high = players[0]
        low = players[1]
        mGame.init()
        wairForStartBets()
    }

    fun blinds(): PokerCard {
        game_stage = GAME_STAGE.BLINDS
        val dropCard = mGame.dropCard()
        droppedCard += dropCard
        mGame.drawCard()
        mGame.drawCard()
        mGame.drawCard()
        return dropCard
    }

    private fun calculateNextPlayerTurn() {
        if (turnPlayer + 1 >= players.size)
            turnPlayer = 0
        else
            turnPlayer += 1

        if (players[turnPlayer].folded)
            calculateNextPlayerTurn()
    }

    fun wairForBets() {
        game_stage = GAME_STAGE.WAIT_FOR_BETS
    }

    fun wairForStartBets() {
        game_stage = GAME_STAGE.START_BETS
    }

    fun call(player: Player) {
        if (game_stage == GAME_STAGE.CALL_RAISE_FOLD ||
                game_stage == GAME_STAGE.WAIT_FOR_BETS) {
            game_stage = GAME_STAGE.CALL_RAISE_FOLD
            calculateNextPlayerTurn()
            val sum = bets.getOrElse(player) { emptyList<Bet>() }.map { it.totalMoney() }.sum()
            placeBet(player, player.placeBet(totalBetToCall - sum))
        }
    }

    fun pass() {
        if (game_stage == GAME_STAGE.CALL_RAISE_FOLD ||
                game_stage == GAME_STAGE.WAIT_FOR_BETS) {
            calculateNextPlayerTurn()
        }
    }

    fun fold(player: Player) {
        if (game_stage == GAME_STAGE.CALL_RAISE_FOLD ||
                game_stage == GAME_STAGE.WAIT_FOR_BETS) {
            player.folded = true
            calculateNextPlayerTurn()
        }
    }

    fun raise(player: Player, amount: Int) {
        if ((game_stage == GAME_STAGE.CALL_RAISE_FOLD ||
                        game_stage == GAME_STAGE.WAIT_FOR_BETS) && currentRaises < MAX_RAISE_NUM) {
            currentRaises += 1
            minCurrentBetAmount = amount
            val sum = bets.getOrElse(player) { emptyList<Bet>() }.map { it.totalMoney() }.sum()
            val bet = player.placeBet((amount + totalBetToCall) - sum)
            totalBetToCall = bet.totalMoney() + sum
            placeBet(player, bet)
            calculateNextPlayerTurn()
        }
    }

    fun high() {
        if (game_stage == GAME_STAGE.START_BETS) {
            val bet = high.placeBet(minStartBetAmount)
            totalBetToCall = bet.totalMoney()
            placeBet(high, bet)
            game_stage = GAME_STAGE.HIGHS
            turnPlayer = 1
        }
    }

    fun low() {
        if (game_stage == GAME_STAGE.HIGHS) {
            val bet = low.placeBet(minStartBetAmount / 2)
            placeBet(low, bet)
            game_stage = GAME_STAGE.LOWS
            turnPlayer = 2
        }
    }

    private fun placeBet(player: Player, bet: Bet): Unit {
        if (bets.containsKey(player)) {
            bets[player]?.add(bet)
        } else {
            bets.put(player, arrayListOf(bet))
        }
        when (game_stage) {
            GAME_STAGE.WAIT_FOR_BETS -> GAME_STAGE.CALL_RAISE_FOLD
        }
        val allBets = mGame.players.filter { !it.folded }
                .map {
                    sumBets(bets[it])
                }.reduceRight { i: Int, acc: Int ->
                    if (acc == i) i else 0
                }

        if (allBets != 0) {
            game_stage = GAME_STAGE.DEALING
        }
    }

    private fun sumBets(bets: MutableList<Bet>?) = bets?.sumBy {
        it.totalMoney()
    } ?: 0


    fun deal(): PokerCard? {
        if (game_stage == GAME_STAGE.DEALING && mGame.state.tableCards.size < MAX_CARDS_TABLE) {
            val dropCard = mGame.dropCard()
            droppedCard += dropCard
            mGame.drawCard()
            game_stage = GAME_STAGE.WAIT_FOR_BETS
            currentRaises = 0
            return dropCard
        }
        return null
    }
}

data class PokerHand(val type: PokerHandType, val priority: Int)

interface ShowdownHand {
    fun analyze(player: Player, mGame: Game): PokerHand?
    fun getType(): PokerHandType
    fun getPriority(): Int
    fun untie(players: List<Player>, mGame: Game): Player?
}

class Showdown(val showdownHands: List<ShowdownHand>, val players: List<Player>, val mGame: Game) {

    init {
        showdownHands.sortedByDescending { it.getPriority() }
    }

    fun ItsGoTime(): Player? {
        val playerHandPair = players.map { player ->
            val hands = showdownHands.mapNotNull {
                it.analyze(player, mGame)
            }.sortedByDescending { it.priority }
            player to hands
        }.sortedByDescending {
            it.second.first().priority
        }
        val bestHandMap = playerHandPair
                .groupBy {
                    it.second.first().priority
                }.toSortedMap(compareByDescending { it })
        val bestHandList = bestHandMap[bestHandMap.firstKey()].orEmpty()

        return if (bestHandList.size == 1) {
            bestHandList.first().first
        } else {
            val untie = getShowdownHandForPokerHand(bestHandList.first().second.first())
                    ?.untie(bestHandList.map { it.first }, mGame)
            untie
        }
    }

    private fun getShowdownHandForPokerHand(pokerhand: PokerHand): ShowdownHand? {
        return showdownHands.find { it.getType() == pokerhand.type }
    }

}

