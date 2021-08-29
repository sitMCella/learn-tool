package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.common.toNullable
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import org.springframework.stereotype.Service

@Service
class CardService(private val cardRepository: CardRepository, private val workspaceRepository: WorkspaceRepository) {

    fun create(workspaceName: String, cardContent: CardContent): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        if (!workspaceRepository.existsById(workspaceName)) {
            throw WorkspaceNotExistsException(workspaceName)
        }
        val cardId = CardIdGenerator.create()
        if (cardRepository.existsById(cardId)) {
            throw CardAlreadyExistsException(cardId)
        }
        val card = Card.create(cardId, workspaceName, cardContent)
        cardRepository.save(card)
        return card
    }

    fun update(cardId: String, workspaceName: String, cardContent: CardContent): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        if (!workspaceRepository.existsById(workspaceName)) {
            throw WorkspaceNotExistsException(workspaceName)
        }
        val card = findById(cardId)
        if (card.workspaceName != workspaceName) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        val updatedCard = Card.create(cardId, workspaceName, cardContent)
        cardRepository.save(updatedCard)
        return updatedCard
    }

    fun delete(cardId: String, workspaceName: String) {
        if (!workspaceRepository.existsById(workspaceName)) {
            throw WorkspaceNotExistsException(workspaceName)
        }
        val card = findById(cardId)
        if (card.workspaceName != workspaceName) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        cardRepository.delete(card)
    }

    fun findByWorkspaceName(workspaceName: String): List<Card> = cardRepository.findByWorkspaceName(workspaceName)

    fun findById(cardId: String): Card = cardRepository.findById(cardId).toNullable() ?: throw CardNotFoundException(cardId)
}
