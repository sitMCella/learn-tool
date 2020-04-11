package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardNotFoundException(workspaceName: String, cardId: String) : ResponseStatusException(HttpStatus.NOT_FOUND, "LearnCard with id $cardId not found in Workspace with name $workspaceName.")
