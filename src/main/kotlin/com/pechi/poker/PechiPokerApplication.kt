package com.pechi.poker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.socket.config.annotation.EnableWebSocket


@SpringBootApplication
@EnableWebSocket
class PechiPokerApplication{

}

fun main(args: Array<String>) {
    runApplication<PechiPokerApplication>(*args)
}
