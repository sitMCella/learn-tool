package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.storage.CardEntity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Card(
    @field:NotNull @field:NotEmpty val id: String,
    @field:NotNull @field:NotEmpty val workspaceName: String,
    @field:NotNull @field:NotEmpty val question: String,
    @field:NotNull @field:NotEmpty val response: String
) {
    companion object {
        fun create(cardEntity: CardEntity): Card {
            return Card(cardEntity.id, cardEntity.workspaceName, cardEntity.question, cardEntity.response)
        }
    }
}
