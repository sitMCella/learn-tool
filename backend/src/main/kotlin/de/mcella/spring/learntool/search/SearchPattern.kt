package de.mcella.spring.learntool.search

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SearchPattern(
    @field:NotNull @field:NotEmpty val content: String = ""
)
