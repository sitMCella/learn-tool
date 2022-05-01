package de.mcella.spring.learntool.security

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Token(
    @field:NotNull @field:NotEmpty val token: String
)
