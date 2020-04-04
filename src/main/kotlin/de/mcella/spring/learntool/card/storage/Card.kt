package de.mcella.spring.learntool.card.storage

import de.mcella.spring.learntool.card.CardContent
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

const val UUID_REGEXP = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

@Entity
@Table(name = "cards")
data class Card(
    @Id @Pattern(regexp = UUID_REGEXP) val id: String = "",
    @field:NotNull @field:NotEmpty val workspaceName: String = "",
    @field:NotNull @field:NotEmpty val question: String = "",
    @field:NotNull @field:NotEmpty val response: String = ""
) {
    companion object {
        fun create(cardId: String, workspaceName: String, cardContent: CardContent): Card {
            return Card(cardId, workspaceName, cardContent.question, cardContent.response)
        }
    }
}
