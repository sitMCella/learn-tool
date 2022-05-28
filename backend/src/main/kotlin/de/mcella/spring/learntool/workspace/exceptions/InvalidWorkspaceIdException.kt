package de.mcella.spring.learntool.workspace.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidWorkspaceIdException(message: String) : ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message)
