package de.mcella.spring.learntool.card

import java.util.UUID

object CardIdGenerator {

    fun create(): String {
        return UUID.randomUUID().toString()
    }
}
