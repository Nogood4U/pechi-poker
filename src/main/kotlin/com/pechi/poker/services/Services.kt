package com.pechi.poker.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.pechi.poker.deck.PokerCard
import com.pechi.poker.game.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import reactor.core.publisher.ConnectableFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct


@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Service
class GameService(@Autowired val playerService: PlayerService) {

    var matches: MutableMap<String, GameMatch> = mutableMapOf()
    var matchFlux: MutableMap<String, Flux<Any>> = mutableMapOf()
    var matchSink: MutableMap<String, FluxSink<Any>> = mutableMapOf()
    var sink: FluxSink<Any>? = null


    @PostConstruct
    fun postConstruct() {
        val flux: Flux<Any> = Flux.create<Any> {
            sink = it
        }
        flux.subscribe { msg ->
            try {
                when (msg) {

                    is Join -> {
                        matches[msg.code]?.join(Player(msg.player.name))
                        matchSink[msg.code]?.next("Joined ${msg.player.name}!!")
                    }
                    is Create -> {
                        matches[msg.code] = GameMatch(listOf(Player(msg.player.name)), msg.code)
                        matchFlux[msg.code] = Flux.create<Any> {
                            matchSink[msg.code] = it
                        }.publish()
                        matchFlux[msg.code]?.subscribe {
                            when (it) {
                                is StartGame -> {
                                    matches[msg.code]?.apply {
                                        start()
                                        high()
                                        low()
                                        blinds()
                                        wairForStartBets()
                                        matchSink[msg.code]?.next(EventProcessor.buildUpdateFromGameMatch(this))
                                    }
                                }
                                is Play -> matchSink[msg.code]?.next(EventProcessor.processPlay(it, matches[it.code()]))
                                else -> println(it)
                            }
                        }
                        (matchFlux[msg.code] as ConnectableFlux).connect()
                    }
                    is Connected -> {
                        matches[msg.code]?.apply {
                            matchSink[msg.code]?.next(EventProcessor.buildUpdateFromGameMatch(this))
                        }

                    }
                    else -> println(msg)
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    fun createMatch(code: String, player: PlayerService.Player) {
        sink?.next(Create(code, player))
    }

    fun startMatch(code: String, player: PlayerService.Player) {
        matchSink[code]?.next(StartGame(code))
    }

    fun joinMatch(code: String, player: PlayerService.Player) {
        sink?.next(Join(code, player))
    }

    fun getGame(code: String): Game? {
        return matches[code]?.let {
            Game(code, it.players.map { PlayerService.Player(it.name) })
        }

    }

    fun getAllGames(): List<Game> {
        return matches.values.map { Game(it.name, it.players.map { PlayerService.Player(it.name) }) }
    }

    var mapper = ObjectMapper()
    fun connect(code: String, player: String): Flux<Any> {
        return matches[code]?.let {
            playerService.get(player)?.let {
                Flux.create<Any> { mySink ->
                    matchFlux[code]?.filter { it is Update }?.subscribe {
                        val node = mapper.valueToTree<ObjectNode>(it)
                        node.get("player").asIterable().filter { it["name"].asText() != player }
                                .forEach { (it as ObjectNode)["cards"] = mapper.valueToTree<ObjectNode>(emptyList<String>()) }

                        // (it as Update).player.filter { p -> p.name != player }.forEach { it.cards = emptyList() }
                        mySink.next(node)
                    }
                    sink?.next(Connected(code, it))
                }.doOnCancel {
                    println("$player Disconnected!!")
                }.doOnTerminate {
                    println("$player Terminated!!")
                }.doOnError {
                    println("$player Error!!")
                }.mergeWith(Flux.interval(Duration.of(10, ChronoUnit.SECONDS))).log()

            }
        } ?: Flux.empty()
    }

    fun leaveGame(code: String, player: String): Boolean {
        return matches[code]?.let { match ->
            playerService.get(player)?.let {
                match.dropPlayer(it.name)
            }
        } ?: false
    }

    fun call(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                matchSink[code]?.next(Call(code, it))
                true
            }
        } ?: false
    }

    fun raise(code: String, player: String, amount: Int): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                if (amount > 0) {
                    matchSink[code]?.next(Raise(code, it, amount))
                    true
                } else false
            }
        } ?: false
    }

    fun fold(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                matchSink[code]?.next(Fold(code, it))
                true
            }
        } ?: false
    }

    fun pass(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                matchSink[code]?.next(Pass(code, it))
                true
            }
        } ?: false
    }

    data class Join(val code: String, val player: PlayerService.Player)
    data class Create(val code: String, val player: PlayerService.Player)
    data class Connected(val code: String, val player: PlayerService.Player)
    data class Game(val name: String, val players: List<PlayerService.Player>?)
    data class Update(val playerTurn: String?, val game_stage: GameMatch.GAME_STAGE?,
                      val tableCards: List<PokerCard>?, val droppedCards: List<PokerCard>?, val winner: Player? = null, var player: List<Player> = emptyList()) {
        constructor() : this(null, null, null, null, null, emptyList())
    }


    data class StartGame(val code: String)

    data class Fold(val code: String, val player: PlayerService.Player) : Play(code) {
        override fun getPlayerName() = player.name
    }

    data class Raise(val code: String, val player: PlayerService.Player, val amount: Int) : Play(code) {
        override fun getPlayerName() = player.name

    }

    data class Call(val code: String, val player: PlayerService.Player) : Play(code) {
        override fun getPlayerName() = player.name

    }

    data class Pass(val code: String, val player: PlayerService.Player) : Play(code) {
        override fun getPlayerName() = player.name

    }

    data class Showdown(val code: String, val player: PlayerService.Player) : Play(code) {
        override fun getPlayerName() = player.name

    }

    abstract class Play(private val code: String) {
        abstract fun getPlayerName(): String
        fun code() = this.code
    }
}

@Service
class PlayerService {

    var players: MutableMap<String, Player> = mutableMapOf()

    fun get(player: String): Player? {
        return players[player]
    }

    fun register(name: String) {
        players[name] = Player(name)
    }

    data class Player(val name: String)
}

object EventProcessor {

    fun processPlay(play: GameService.Play, game: GameMatch?): GameService.Update {
        val nextTurnPlayer = game!!.nextTurn()
        return if (nextTurnPlayer.name == play.getPlayerName())
            when (play) {
                is GameService.Fold -> {
                    game.apply {
                        fold(nextTurnPlayer)
                        deal()
                    }
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Pass -> {
                    game.pass()
                    game.deal()
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Call -> {
                    game.call(nextTurnPlayer)
                    game.deal()
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Raise -> {
                    game.raise(nextTurnPlayer, play.amount)
                    game.deal()
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Showdown -> {
                    val showdown = Showdown(listOf(HighCard(), OnePair(), TwoPair(), ThreeOfaKind()), game.players, game.mGame)
                    buildUpdateForRoundWinner(game, showdown.ItsGoTime())
                }
                else -> GameService.Update()
            }
        else
            GameService.Update()
    }


    fun buildUpdateFromGameMatch(gameMatch: GameMatch): GameService.Update {
        val nextTurnPlayer = gameMatch.nextTurn()
        val stage = gameMatch.game_stage
        val tableCards = gameMatch.mGame.state.tableCards
        val droppedCard = gameMatch.droppedCard
        return GameService.Update(nextTurnPlayer.name, stage, tableCards, droppedCard, null, gameMatch.players)
    }

    fun buildUpdateForRoundWinner(gameMatch: GameMatch, winner: Player?): GameService.Update {
        val nextTurnPlayer = gameMatch.nextTurn()
        val stage = gameMatch.game_stage
        val tableCards = gameMatch.mGame.state.tableCards
        val droppedCard = gameMatch.droppedCard
        return GameService.Update(nextTurnPlayer.name, stage, tableCards, droppedCard, winner, gameMatch.players)
    }
}
