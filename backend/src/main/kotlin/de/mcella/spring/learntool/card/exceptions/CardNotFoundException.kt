package de.mcella.spring.learntool.card.exceptions

import de.mcella.spring.learntool.card.CardId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardNotFoundException(cardId: CardId) : ResponseStatusException(HttpStatus.NOT_FOUND, "Card with id ${cardId.id} not found.")
