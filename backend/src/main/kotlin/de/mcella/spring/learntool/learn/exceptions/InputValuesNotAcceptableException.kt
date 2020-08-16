package de.mcella.spring.learntool.learn.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InputValuesNotAcceptableException(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)
