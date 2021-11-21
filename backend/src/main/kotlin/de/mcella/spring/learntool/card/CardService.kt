package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.streams.toList
import org.springframework.stereotype.Service

@Service
class CardService(private val cardRepository: CardRepository, private val workspaceRepository: WorkspaceRepository, private val cardIdGenerator: CardIdGenerator) {

    fun create(workspace: Workspace, cardContent: CardContent): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        if (!workspaceRepository.existsById(workspace.name)) {
            throw WorkspaceNotExistsException(workspace)
        }
        val cardId = cardIdGenerator.create()
        if (cardRepository.existsById(cardId.id)) {
            throw CardAlreadyExistsException(cardId)
        }
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        return Card.create(cardRepository.save(cardEntity))
    }

    fun update(cardId: CardId, workspace: Workspace, cardContent: CardContent): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        if (!workspaceRepository.existsById(workspace.name)) {
            throw WorkspaceNotExistsException(workspace)
        }
        val card = findById(cardId)
        if (card.workspaceName != workspace.name) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        val updatedCard = CardEntity.create(cardId, workspace, cardContent)
        val updatedCardEntity = cardRepository.save(updatedCard)
        return Card.create(updatedCardEntity)
    }

    fun delete(cardId: CardId, workspace: Workspace) {
        if (!workspaceRepository.existsById(workspace.name)) {
            throw WorkspaceNotExistsException(workspace)
        }
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        if (cardEntity.workspaceName != workspace.name) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        cardRepository.delete(cardEntity)
    }

    fun findByWorkspace(workspace: Workspace): List<Card> {
        return cardRepository.findByWorkspaceName(workspace.name).stream()
                .map { cardEntity -> Card.create(cardEntity) }
                .toList()
    }

    fun findById(cardId: CardId): Card {
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        return Card.create(cardEntity)
    }
}
