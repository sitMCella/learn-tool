package de.mcella.spring.learntool.card.storage

import de.mcella.spring.learntool.card.Card
import de.mcella.spring.learntool.card.CardContent
import de.mcella.spring.learntool.card.CardId
import de.mcella.spring.learntool.workspace.Workspace
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField

const val UUID_REGEXP = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
const val QUESTION = "question"
const val RESPONSE = "response"

@Entity
@Indexed
@Table(name = "cards")
data class CardEntity(
    @Id @Pattern(regexp = UUID_REGEXP) val id: String = "",
    @KeywordField
    @field:NotNull @field:NotEmpty val workspaceName: String = "",
    @FullTextField
    @field:NotNull @field:NotEmpty val question: String = "",
    @FullTextField
    @field:NotNull @field:NotEmpty val response: String = ""
) {
    companion object {
        fun create(cardId: CardId, workspace: Workspace, cardContent: CardContent): CardEntity {
            return CardEntity(cardId.id, workspace.name, cardContent.question, cardContent.response)
        }

        fun create(card: Card): CardEntity {
            return CardEntity(card.id, card.workspaceName, card.question, card.response)
        }
    }
}
