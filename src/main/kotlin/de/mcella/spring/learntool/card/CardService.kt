package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceDoesNotExistException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import org.springframework.stereotype.Service

@Service
class CardService(private val cardRepository: CardRepository, private val workspaceRepository: WorkspaceRepository) {

    fun create(workspaceName: String, cardContent: CardContent): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        if (!workspaceRepository.existsById(workspaceName)) {
            throw WorkspaceDoesNotExistException(workspaceName)
        }
        val cardId = CardIdGenerator.create()
        if (cardRepository.existsById(cardId)) {
            throw CardAlreadyExistsException(
                cardId
            )
        }
        val card = Card.create(cardId, workspaceName, cardContent)
        cardRepository.save(card)
        return card
    }
}
