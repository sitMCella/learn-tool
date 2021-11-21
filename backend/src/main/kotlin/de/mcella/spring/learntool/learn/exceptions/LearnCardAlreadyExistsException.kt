package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.card.CardId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardAlreadyExistsException(cardId: CardId) : ResponseStatusException(HttpStatus.CONFLICT, "A LearnCard with id ${cardId.id} already exists.")
