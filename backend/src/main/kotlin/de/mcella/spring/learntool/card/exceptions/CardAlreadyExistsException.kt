package de.mcella.spring.learntool.card.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardAlreadyExistsException(cardId: String) : ResponseStatusException(HttpStatus.CONFLICT, "A Card with id $cardId already exists.")
