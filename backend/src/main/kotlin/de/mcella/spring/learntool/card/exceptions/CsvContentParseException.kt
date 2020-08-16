package de.mcella.spring.learntool.card.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CsvContentParseException(message: String? = "") : ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot parse CSV content. $message")
