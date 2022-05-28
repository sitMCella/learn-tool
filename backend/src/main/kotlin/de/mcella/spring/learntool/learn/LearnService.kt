package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.common.toNullable
import de.mcella.spring.learntool.learn.algorithm.InputValues
import de.mcella.spring.learntool.learn.algorithm.Sm2Algorithm
import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.streams.toList
import org.springframework.stereotype.Service

@Service
class LearnService(
    private val cardService: CardService,
    private val workspaceService: WorkspaceService,
    private val learnCardRepository: LearnCardRepository
) {

    fun create(workspaceRequest: WorkspaceRequest, cardId: CardId, userPrincipal: UserPrincipal): LearnCard {
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        verifyIfLearnCardAlreadyExists(cardId)
        val card = cardService.findById(cardId)
        if (!Card.hasWorkspaceId(card, workspaceRequest)) {
            throw CardBindingException(workspaceRequest, cardId)
        }
        val initialLearnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        return LearnCard.create(learnCardRepository.save(initialLearnCard))
    }

    fun create(learnCard: LearnCard): LearnCard {
        verifyIfLearnCardAlreadyExists(learnCard)
        return LearnCard.create(learnCardRepository.save(LearnCardEntity.create(learnCard)))
    }

    fun getCard(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): Card {
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        val learnCard = learnCardRepository.findFirstByWorkspaceIdAndNextReviewBeforeOrderByNextReview(workspaceRequest.id, end).toNullable() ?: throw LearnCardsNotFoundException(workspaceRequest)
        return cardService.findById(CardId(learnCard.id))
    }

    fun evaluateCard(workspaceRequest: WorkspaceRequest, cardId: CardId, evaluationParameters: EvaluationParameters, userPrincipal: UserPrincipal): LearnCard {
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val card = cardService.findById(cardId)
        if (!Card.hasWorkspaceId(card, workspaceRequest)) {
            throw CardBindingException(workspaceRequest, cardId)
        }
        val learnCard = learnCardRepository.findById(cardId.id).toNullable() ?: throw LearnCardNotFoundException(workspaceRequest, cardId)
        val inputValues = InputValues.create(evaluationParameters, learnCard)
        Sm2Algorithm.validate(inputValues)
        val outputValues = Sm2Algorithm.evaluate(inputValues)
        val updatedLearnCard = LearnCardEntity.create(cardId, workspaceRequest, outputValues, Instant.now())
        return LearnCard.create(learnCardRepository.save(updatedLearnCard))
    }

    fun getLearnCardsByWorkspace(workspaceRequest: WorkspaceRequest): List<LearnCard> {
        return learnCardRepository.findByWorkspaceId(workspaceRequest.id).stream()
                .map { learnCardEntity -> LearnCard.create(learnCardEntity) }
                .toList()
    }

    fun delete(workspaceRequest: WorkspaceRequest, cardId: CardId, userPrincipal: UserPrincipal) {
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val card = cardService.findById(cardId)
        if (!Card.hasWorkspaceId(card, workspaceRequest)) {
            throw CardBindingException(workspaceRequest, cardId)
        }
        val learnCard = learnCardRepository.findById(cardId.id).toNullable() ?: throw LearnCardNotFoundException(workspaceRequest, cardId)
        learnCardRepository.delete(learnCard)
    }

    private fun verifyIfLearnCardAlreadyExists(cardId: CardId) {
        if (learnCardRepository.existsById(cardId.id)) {
            throw LearnCardAlreadyExistsException(cardId)
        }
    }

    private fun verifyIfLearnCardAlreadyExists(learnCard: LearnCard) {
        val cardId = CardId(learnCard.id)
        verifyIfLearnCardAlreadyExists(cardId)
    }
}
