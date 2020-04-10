package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardsNotFoundException(workspaceName: String) : ResponseStatusException(HttpStatus.NOT_FOUND, "No Cards found in Workspace with name $workspaceName.")
