package de.mcella.spring.learntool.user

import de.mcella.spring.learntool.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotFoundException(userPrincipal: UserPrincipal) : ResponseStatusException(HttpStatus.NOT_FOUND, "User with id ${userPrincipal.id} not found.")
