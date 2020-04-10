package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardNotFoundException(workspaceName: String, cardId: String) : ResponseStatusException(HttpStatus.NOT_FOUND, "Card with id $cardId not found in Workspace with name $workspaceName.")
