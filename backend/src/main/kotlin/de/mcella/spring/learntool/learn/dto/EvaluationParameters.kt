package de.mcella.spring.learntool.learn.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class EvaluationParameters(
    @field:NotNull @field:NotEmpty val quality: Int = 0
)
