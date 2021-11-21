package de.mcella.spring.learntool.card

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CardId(
    @field:NotNull @field:NotEmpty val id: String
)
