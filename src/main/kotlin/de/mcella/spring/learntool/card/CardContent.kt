package de.mcella.spring.learntool.card

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CardContent(
    @field:NotNull @field:NotEmpty val question: String = "",
    @field:NotNull @field:NotEmpty val response: String = ""
)
