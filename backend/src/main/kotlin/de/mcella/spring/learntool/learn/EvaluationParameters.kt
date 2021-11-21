package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardId
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class EvaluationParameters(
    @field:NotNull @field:NotEmpty val cardId: CardId,
    @field:NotNull @field:NotEmpty val quality: Int = 0
)
