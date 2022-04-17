package de.mcella.spring.learntool.card.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CardId(
    @field:NotNull @field:NotEmpty val id: String
)
