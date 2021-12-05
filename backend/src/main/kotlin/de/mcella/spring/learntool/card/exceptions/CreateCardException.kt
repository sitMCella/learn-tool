package de.mcella.spring.learntool.card.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CreateCardException(e: Throwable) : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating the Card. Exception: $e")
