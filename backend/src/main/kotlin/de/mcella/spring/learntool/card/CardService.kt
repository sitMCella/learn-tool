package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.dto.CardPagination
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceIdException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import kotlin.streams.toList
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CardService(private val cardRepository: CardRepository, private val workspaceService: WorkspaceService, private val cardIdGenerator: CardIdGenerator) {

    fun create(workspaceRequest: WorkspaceRequest, cardContent: CardContent, userPrincipal: UserPrincipal): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        verifyIfWorkspaceExists(workspaceRequest)
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardId = cardIdGenerator.create()
        if (cardRepository.existsById(cardId.id)) {
            throw CardAlreadyExistsException(cardId)
        }
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        return Card.create(cardRepository.save(cardEntity))
    }

    fun create(card: Card, userPrincipal: UserPrincipal): Card {
        require(!card.id.isNullOrEmpty()) { "The field 'id' is required." }
        require(!card.workspaceId.isNullOrEmpty()) { "The field 'workspaceId' is required." }
        require(!card.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!card.response.isNullOrEmpty()) { "The field 'response' is required." }
        val workspaceRequest = WorkspaceRequest(card.workspaceId)
        verifyIfWorkspaceExists(workspaceRequest)
        val cardId = CardId(card.id)
        if (cardRepository.existsById(cardId.id)) {
            throw CardAlreadyExistsException(cardId)
        }
        val cardEntity = CardEntity.create(card)
        return Card.create(cardRepository.save(cardEntity))
    }

    fun update(cardId: CardId, workspaceRequest: WorkspaceRequest, cardContent: CardContent, userPrincipal: UserPrincipal): Card {
        require(!cardContent.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!cardContent.response.isNullOrEmpty()) { "The field 'response' is required." }
        verifyIfWorkspaceExists(workspaceRequest)
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        if (!CardEntity.hasWorkspaceId(cardEntity, workspaceRequest)) {
            throw InvalidWorkspaceIdException("The provided workspace id does not match with the card workspace id")
        }
        val cardCreationDate = CardCreationDate.create(cardEntity)
        val updatedCard = CardEntity.create(cardId, workspaceRequest, cardContent, cardCreationDate.creationDate)
        val updatedCardEntity = cardRepository.save(updatedCard)
        return Card.create(updatedCardEntity)
    }

    fun delete(cardId: CardId, workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal) {
        verifyIfWorkspaceExists(workspaceRequest)
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        if (!CardEntity.hasWorkspaceId(cardEntity, workspaceRequest)) {
            throw InvalidWorkspaceIdException("The provided workspace id does not match with the card workspace id")
        }
        cardRepository.delete(cardEntity)
    }

    fun findByWorkspace(workspaceRequest: WorkspaceRequest, cardPagination: CardPagination?, userPrincipal: UserPrincipal): List<Card> {
        verifyIfWorkspaceExists(workspaceRequest)
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val pageRequest = cardPagination?.let {
            PageRequest.of(cardPagination.page, cardPagination.size)
        } ?: Pageable.unpaged()
        return cardRepository.findByWorkspaceIdOrderByCreationDateDesc(workspaceRequest.id, pageRequest).stream()
                .map { cardEntity -> Card.create(cardEntity) }
                .toList()
    }

    fun countByWorkspace(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): Long {
        verifyIfWorkspaceExists(workspaceRequest)
        return cardRepository.countByWorkspaceId(workspaceRequest.id)
    }

    fun findById(cardId: CardId): Card {
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        return Card.create(cardEntity)
    }

    private fun verifyIfWorkspaceExists(workspaceRequest: WorkspaceRequest) {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
    }
}
