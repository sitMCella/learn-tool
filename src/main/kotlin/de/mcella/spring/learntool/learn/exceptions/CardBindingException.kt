package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardBindingException(workspaceName: String, cardId: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The Card with id $cardId does not belong to Workspace with name $workspaceName.")
