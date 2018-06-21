package com.pechi.poker.services

import com.pechi.poker.deck.PokerCard
import com.pechi.poker.game.GameMatch
import com.pechi.poker.game.Player
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
                        matches[msg.code] = GameMatch(listOf(Player(msg.player.name)))
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
                                        wairForBets()
                                        matchSink[msg.code]?.next(EventProcessor.buildUpdateFromGameMatch(this))
                                    }
                                }
                                is Play -> sink?.next(EventProcessor.processPlay(it, matches[it.code()]))
                                else -> println(it)
                            }
                        }
                        (matchFlux[msg.code] as ConnectableFlux).connect()
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

    fun connect(code: String, player: String): Flux<Any> {
        return matches[code]?.let {
            playerService.get(player)?.let {
                Flux.create<Any> { mySink ->
                    matchFlux[code]?.subscribe {
                        mySink.next(it)
                    }
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

    fun call(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                sink?.next(Call(code, it))
                true
            }
        } ?: false
    }

    fun raise(code: String, player: String, amount: Int): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                sink?.next(Raise(code, it, amount))
                true
            }
        } ?: false
    }

    fun fold(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                sink?.next(Fold(code, it))
                true
            }
        } ?: false
    }

    fun pass(code: String, player: String): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                sink?.next(Pass(code, it))
                true
            }
        } ?: false
    }

    data class Join(val code: String, val player: PlayerService.Player)
    data class Create(val code: String, val player: PlayerService.Player)
    data class Game(val name: String, val players: List<PlayerService.Player>?)

    data class Update(val playerTurn: String?, val game_stage: GameMatch.GAME_STAGE?, val tableCards: List<PokerCard>?) {
        constructor() : this(null, null, null)
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
                    game.fold(nextTurnPlayer)
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Pass -> {
                    game.pass()
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Call -> {
                    game.fold(nextTurnPlayer)
                    buildUpdateFromGameMatch(game)
                }
                is GameService.Raise -> {
                    game.fold(nextTurnPlayer)
                    buildUpdateFromGameMatch(game)
                }
                else -> GameService.Update()
            }
        else
            GameService.Update()
    }


    fun buildUpdateFromGameMatch(gameMatch: GameMatch): GameService.Update {
        val nextTurnPlayer = gameMatch.nextTurn()
        val game_stage = gameMatch.game_stage
        val tableCards = gameMatch.mGame.state.tableCards
        return GameService.Update(nextTurnPlayer.name, game_stage, tableCards)
    }
}
