package de.mcella.spring.learntool.card.dto

import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Card(
    @field:NotNull @field:NotEmpty val id: String,
    @field:NotNull @field:NotEmpty val workspaceId: String,
    @field:NotNull @field:NotEmpty val question: String,
    @field:NotNull @field:NotEmpty val response: String
) {
    companion object {
        fun create(cardEntity: CardEntity): Card {
            return Card(cardEntity.id, cardEntity.workspaceId, cardEntity.question, cardEntity.response)
        }

        fun hasWorkspaceId(card: Card, workspaceRequest: WorkspaceRequest): Boolean {
            return card.workspaceId == workspaceRequest.id
        }
    }
}
