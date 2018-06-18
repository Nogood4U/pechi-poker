package com.pechi.poker.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class IndexController {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/join/{code}")
    fun join(): String {
        return "index"
    }

    @PostMapping("/create/{code}")
    fun create(): String {
        return "index"
    }
}