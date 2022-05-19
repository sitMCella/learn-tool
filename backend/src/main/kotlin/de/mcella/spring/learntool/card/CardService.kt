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
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
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
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardId = cardIdGenerator.create()
        if (cardRepository.existsById(cardId.id)) {
            throw CardAlreadyExistsException(cardId)
        }
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        return Card.create(cardRepository.save(cardEntity))
    }

    fun create(card: Card): Card {
        require(!card.id.isNullOrEmpty()) { "The field 'id' is required." }
        require(!card.workspaceName.isNullOrEmpty()) { "The field 'workspaceName' is required." }
        require(!card.question.isNullOrEmpty()) { "The field 'question' is required." }
        require(!card.response.isNullOrEmpty()) { "The field 'response' is required." }
        val workspaceRequest = WorkspaceRequest(card.workspaceName)
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
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
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        val cardCreationDate = CardCreationDate.create(cardEntity)
        if (cardCreationDate.card.workspaceName != workspaceRequest.name) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        val updatedCard = CardEntity.create(cardId, workspaceRequest, cardContent, cardCreationDate.creationDate)
        val updatedCardEntity = cardRepository.save(updatedCard)
        return Card.create(updatedCardEntity)
    }

    fun delete(cardId: CardId, workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal) {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        if (cardEntity.workspaceName != workspaceRequest.name) {
            throw InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace")
        }
        cardRepository.delete(cardEntity)
    }

    fun findByWorkspace(workspaceRequest: WorkspaceRequest, cardPagination: CardPagination?, userPrincipal: UserPrincipal): List<Card> {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val pageRequest = cardPagination?.let {
            PageRequest.of(cardPagination.page, cardPagination.size)
        } ?: Pageable.unpaged()
        return cardRepository.findByWorkspaceNameOrderByCreationDateDesc(workspaceRequest.name, pageRequest).stream()
                .map { cardEntity -> Card.create(cardEntity) }
                .toList()
    }

    fun countByWorkspace(workspaceRequest: WorkspaceRequest): Long {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        return cardRepository.countByWorkspaceName(workspaceRequest.name)
    }

    fun findById(cardId: CardId): Card {
        val cardEntity = cardRepository.findById(cardId.id).orElseThrow { CardNotFoundException(cardId) }
        return Card.create(cardEntity)
    }
}
