package com.pechi.poker.controllers

import com.pechi.poker.services.GameService
import com.pechi.poker.services.PlayerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URI

@Controller
@RequestMapping("/game", produces = [(MediaType.APPLICATION_JSON_VALUE)])
class IndexController(@Autowired val gameService: GameService, @Autowired val playerService: PlayerService) {

    @GetMapping("/{code}")
    fun index(@PathVariable("code") code: String): ResponseEntity<GameService.Game> {
        return ResponseEntity.ok(gameService.getGame(code))
    }

    @GetMapping("/join/{code}")
    fun join(@PathVariable("code") code: String, @RequestBody player: String): ResponseEntity<String> {
        gameService.joinMatch(code, playerService.get(player))
        return ResponseEntity.ok().build()
    }

    @PostMapping("/create/{code}")
    fun create(@PathVariable("code") code: String, @RequestBody player: String): ResponseEntity<String> {
        gameService.createMatch(code, playerService.get(player))
        return ResponseEntity.created(URI("")).build<String>()
    }

}