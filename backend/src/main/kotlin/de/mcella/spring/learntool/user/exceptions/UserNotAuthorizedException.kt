package de.mcella.spring.learntool.user.exceptions

import de.mcella.spring.learntool.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotAuthorizedException(userPrincipal: UserPrincipal) : ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized action for the user with id ${userPrincipal.id}.")
