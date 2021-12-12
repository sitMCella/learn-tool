package de.mcella.spring.learntool.card.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CardPagination(
    @field:NotNull @field:NotEmpty val page: Int = 0,
    @field:NotNull @field:NotEmpty val size: Int = 0
)
