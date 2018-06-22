package com.pechi.poker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.util.concurrent.TimeUnit


@SpringBootApplication
@EnableWebFlux
class PechiPokerApplication : WebFluxConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/static", "classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    }
}

fun main(args: Array<String>) {
    runApplication<PechiPokerApplication>(*args)
}
