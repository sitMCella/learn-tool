package de.mcella.spring.learntool

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [RestClientAutoConfiguration::class])
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
