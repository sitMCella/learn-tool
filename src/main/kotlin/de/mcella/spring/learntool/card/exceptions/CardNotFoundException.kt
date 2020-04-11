package de.mcella.spring.learntool.card.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardNotFoundException(cardId: String) : ResponseStatusException(HttpStatus.NOT_FOUND, "Card with id $cardId not found.")
