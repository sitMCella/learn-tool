package de.mcella.spring.learntool.card.exceptions

import de.mcella.spring.learntool.card.CardId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UpdateCardException(cardId: CardId, e: Throwable) : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating the Card with id ${cardId.id}. Exception: $e")
