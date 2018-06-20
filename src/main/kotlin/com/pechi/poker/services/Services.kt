package com.pechi.poker.services

import com.pechi.poker.game.GameMatch
import com.pechi.poker.game.Player
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import javax.annotation.PostConstruct
import javax.websocket.Session

@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Service
class GameService(@Autowired val playerService: PlayerService) {

    var matches: MutableMap<String, GameMatch> = mutableMapOf()
    var conns: MutableMap<String, Pair<PlayerService.Player, Session>> = mutableMapOf()
    var sink: FluxSink<Any>? = null;


    @PostConstruct
    fun postConstruct() {
        var flux: Flux<Any> = Flux.create<Any> {
            sink = it
        }
        flux.subscribe { msg->
            try {
                when (msg) {
                    is Join -> {
                        matches[msg.code]?.join(Player(msg.player.name))
                        conns.values.forEach { it.second.basicRemote.sendText("Joined ${msg.player.name}!!") }
                    }
                    is Create -> {
                        matches[msg.code] = GameMatch(listOf(Player(msg.player.name)))
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

    fun joinMatch(code: String, player: PlayerService.Player) {
        sink?.next(Join(code, player))
    }

    fun getGame(code: String): Game? {
        return matches[code]?.let {
            Game(code, it.players.map { PlayerService.Player(it.name) })
        }

    }

    fun connect(code: String, player: String, session: Session): Boolean {
        return matches[code]?.let {
            playerService.get(player)?.let {
                conns[code] = it to session
                true
            }
        } ?: false
    }

    data class Join(val code: String, val player: PlayerService.Player)
    data class Create(val code: String, val player: PlayerService.Player)

    data class Game(val name: String, val players: List<PlayerService.Player>?)
}

@Service
class PlayerService {

    var players: MutableMap<String, Player> = mutableMapOf()

    fun get(player: String): Player? {
        return players[player]
    }

    fun register(name: String): Unit {
        players[name] = Player(name)
    }

    data class Player(val name: String)
}
