package de.mcella.spring.learntool.user.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SignUpRequest(
    @field:NotNull @field:NotEmpty val name: String,
    @field:NotNull @field:NotEmpty @field:Email val email: String,
    @field:NotNull @field:NotEmpty val password: String
)
