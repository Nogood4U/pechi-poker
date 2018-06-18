package com.pechi.poker.services

import com.pechi.poker.game.GameMatch
import com.pechi.poker.game.Player
import reactor.core.publisher.ConnectableFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink


class GameService {

    var matches: MutableMap<String, GameMatch> = mutableMapOf()
    var sink: FluxSink<Any>? = null;
    var flux: ConnectableFlux<Any> = Flux.create<Any> {
        sink = it
    }.publish()

    init {
        flux.subscribe {
            when (it) {
                is Join -> {
                    matches[it.code]?.join(it.player)
                }
                is Create -> {
                    matches[it.code] = GameMatch(listOf(it.player))
                }
            }
        }
    }

    fun createMatch(code: String, player: Player) {
        sink?.next(Create(code, player))
    }

    fun joinMatch(code: String, player: Player) {
        sink?.next(Join(code, player))
    }

    data class Join(val code: String, val player: Player)
    data class Create(val code: String, val player: Player)
}


