package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardAlreadyExistsException(cardId: String) : ResponseStatusException(HttpStatus.CONFLICT, "A LearnCard with id $cardId already exists.")
