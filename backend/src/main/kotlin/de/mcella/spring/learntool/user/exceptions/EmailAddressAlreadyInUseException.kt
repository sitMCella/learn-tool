package de.mcella.spring.learntool.user.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class EmailAddressAlreadyInUseException : ResponseStatusException(HttpStatus.BAD_REQUEST, "Email address already in use.")
