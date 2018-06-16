package com.pechi.poker

import com.pechi.poker.deck.PokerCard
import com.pechi.poker.deck.PokerDeck
import com.pechi.poker.deck.PokerHandType
import com.pechi.poker.game.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class PechiPokerApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun testGame(): Unit {
        val list = listOf<Player>(
                Player("P1", emptyList(), PokerHand(PokerHandType.CARTA_ALTA,0)),
                Player("P2", emptyList(), PokerHand(PokerHandType.CARTA_ALTA,0)),
                Player("P3", emptyList(), PokerHand(PokerHandType.CARTA_ALTA,0))

        )
        val gameMatch = GameMatch(list)
        println("Game Deck Size ${gameMatch.mGame.deck.mdeck.size}")
        gameMatch.start()
        printLnPlayers(gameMatch.players)
        println("Game Deck Size ${gameMatch.mGame.deck.mdeck.size}")
        gameMatch.high()
        gameMatch.low()
        println()
        println("------------------")
        println("Dealing Blinds")
        gameMatch.blinds()
        println("Game Deck Size After Blinds ${gameMatch.mGame.deck.mdeck.size}")
        println("Discarded Cards")
        printCards(gameMatch.droppedCard)
        println("------------------")
        println("Cards in PLay")
        printCards(gameMatch.mGame.state.tableCards)
        gameMatch.wairForBets()
        var player = gameMatch.players[gameMatch.turnPlayer]
        println("Player ${player.name} Raising...")
        gameMatch.raise(player, gameMatch.minStartBetAmount + 10)
        player = gameMatch.players[gameMatch.turnPlayer]
        println("Player ${player.name} Calling...")
        gameMatch.call(player)
        player = gameMatch.players[gameMatch.turnPlayer]
        println("Player ${player.name} Raising...")
        gameMatch.raise(player, gameMatch.minCurrentBetAmount)
        player = gameMatch.players[gameMatch.turnPlayer]
        println("Player ${player.name} Calling...")
        gameMatch.call(player)
        player = gameMatch.players[gameMatch.turnPlayer]
        println("Player ${player.name} Calling...")
        gameMatch.call(player)
        println("-----------------------")
        println(gameMatch.game_stage)
        println("-----------------------")
        // gameMatch.deal()
        println("------------------")
        printCards(gameMatch.mGame.state.tableCards)
        println("-----------------------")
        println("Discarded Cards")
        printCards(gameMatch.droppedCard)
        println("-----------------------")
        println("Cards in PLay")
        printCards(gameMatch.mGame.state.tableCards)
        println("-----------------------")
        println(gameMatch.game_stage)
        println("-----------------------")
        printBets(gameMatch.bets)
        println("-----------------------")
        println(gameMatch.game_stage)
        println("-----------------------")
        val showdown = Showdown(listOf(HighCard(),OnePair()), gameMatch.players, gameMatch.mGame)
        val player1: Player? = showdown.ItsGoTime()
        println("-----------------------")
        println("Winner was ${player1?.name}")
        println("-----------------------")
    }

    fun printBets(bets: MutableMap<Player, MutableList<Bet>>): Unit {
        println("Player          Bets")
        bets.forEach {
            print(it.key.name.padEnd(16))
            it.value.forEach {
                print("$it")
            }
            println()
        }
    }

    fun printDeck(deck: PokerDeck): Unit {
        printCards(deck.mdeck)
    }

    fun printCards(cards: Iterable<PokerCard>): Unit {
        println("#      Denom")
        cards.forEach {
            println("${it.number} de ${it.suits} ")
        }
        println()
    }

    fun printLnPlayers(players: List<Player>): Unit {
        println("Player      Cards")
        players.forEach {
            print("${it.name.padEnd(13)}")
            it.cards.forEach {
                print("${it.number} de ${it.suits}  -- ")
            }
            println()
        }
    }

}
