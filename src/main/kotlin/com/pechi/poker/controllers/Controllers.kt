package com.pechi.poker.controllers

import com.pechi.poker.services.GameService
import com.pechi.poker.services.PlayerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/game", produces = [(MediaType.APPLICATION_JSON_VALUE)])
class IndexController(@Autowired val gameService: GameService, @Autowired val playerService: PlayerService) {

    @GetMapping("/{code}")
    fun index(@PathVariable("code") code: String): ResponseEntity<GameService.Game>? {
        val game = gameService.getGame(code)
        return if (game != null) ResponseEntity.ok(game) else ResponseEntity.notFound().build()
    }

    @GetMapping("/join/{code}")
    fun join(@PathVariable("code") code: String, @RequestParam("player") player: String): ResponseEntity<String> {
        val let: String? = playerService.get(player)?.let {
            gameService.joinMatch(code, it)
            code
        }
        return if (let != null) ResponseEntity.ok().build() else ResponseEntity.badRequest().build()
    }

    @PostMapping("/create/{code}")
    fun create(@PathVariable("code") code: String, @RequestParam("player") player: String): ResponseEntity<String> {
        val let: String? = playerService.get(player)?.let {
            gameService.createMatch(code, it)
            code
        }
        return if (let != null) ResponseEntity.ok().build() else ResponseEntity.badRequest().build()
    }
}


@Controller
@RequestMapping("/player", produces = [(MediaType.APPLICATION_JSON_VALUE)])
class PlayerController(@Autowired val gameService: GameService, @Autowired val playerService: PlayerService) {

    @PostMapping("create")
    fun create(@RequestParam("name") name: String): ResponseEntity<String> {
        playerService.register(name)
        return ResponseEntity.ok().build()
    }
}