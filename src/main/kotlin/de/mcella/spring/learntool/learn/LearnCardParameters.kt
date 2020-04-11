package de.mcella.spring.learntool.learn

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class LearnCardParameters(
    @field:NotNull @field:NotEmpty val cardId: String = ""
)
