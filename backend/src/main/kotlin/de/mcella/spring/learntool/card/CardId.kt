package de.mcella.spring.learntool.card

import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CardId(
    @field:NotNull @field:NotEmpty @get:JsonValue val id: String
)
