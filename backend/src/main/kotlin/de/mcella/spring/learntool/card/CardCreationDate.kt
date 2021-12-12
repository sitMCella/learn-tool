package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.storage.CardEntity
import java.time.Instant
import javax.validation.constraints.NotNull

data class CardCreationDate(
    @field:NotNull val card: Card,
    @field:NotNull val creationDate: Instant
) {
    companion object {
        fun create(cardEntity: CardEntity): CardCreationDate {
            return CardCreationDate(Card(cardEntity.id, cardEntity.workspaceName, cardEntity.question, cardEntity.response), cardEntity.creationDate)
        }
    }
}
