package de.mcella.spring.learntool.workspace

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidWorkspaceNameException(message: String) : ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message)
