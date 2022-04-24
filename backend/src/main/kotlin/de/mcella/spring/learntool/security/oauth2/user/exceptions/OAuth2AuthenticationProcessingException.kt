package de.mcella.spring.learntool.security.oauth2.user.exceptions

import org.springframework.security.core.AuthenticationException

class OAuth2AuthenticationProcessingException(msg: String) : AuthenticationException(msg)
