package de.mcella.spring.learntool.card.exceptions

import de.mcella.spring.learntool.card.CardId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardAlreadyExistsException(cardId: CardId) : ResponseStatusException(HttpStatus.CONFLICT, "A Card with id ${cardId.id} already exists.")
