package de.mcella.spring.learntool.config

data class Auth(
    var tokenSecret: String,
    var tokenExpirationMsec: Long
)
