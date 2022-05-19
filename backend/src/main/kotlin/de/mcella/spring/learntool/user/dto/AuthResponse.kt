package de.mcella.spring.learntool.user.dto

import de.mcella.spring.learntool.security.Token
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class AuthResponse(
    @field:NotNull @field:NotEmpty val accessToken: String,
    val tokenType: String
) {
    companion object {
        fun create(accessToken: Token): AuthResponse {
            return AuthResponse(accessToken.token, "Bearer")
        }
    }
}
