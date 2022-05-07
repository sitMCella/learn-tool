package de.mcella.spring.learntool.user.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotExistentException : ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
