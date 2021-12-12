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
import de.mcella.spring.learntool.workspace.dto.Workspace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.streams.toList
import org.springframework.stereotype.Service

@Service
class LearnService(private val cardService: CardService, private val learnCardRepository: LearnCardRepository) {

    fun create(workspace: Workspace, cardId: CardId): LearnCard {
        if (learnCardRepository.existsById(cardId.id)) {
            throw LearnCardAlreadyExistsException(cardId)
        }
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspace.name) throw CardBindingException(workspace, cardId)
        return LearnCard.create(learnCardRepository.save(LearnCardEntity.createInitial(cardId, workspace, Instant.now())))
    }

    fun create(learnCard: LearnCard): LearnCard {
        if (learnCardRepository.existsById(learnCard.id)) {
            throw LearnCardAlreadyExistsException(CardId(learnCard.id))
        }
        return LearnCard.create(learnCardRepository.save(LearnCardEntity.create(learnCard)))
    }

    fun getCard(workspace: Workspace): Card {
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        val learnCard = learnCardRepository.findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspace.name, end).toNullable() ?: throw LearnCardsNotFoundException(workspace)
        return cardService.findById(CardId(learnCard.id))
    }

    fun evaluateCard(workspace: Workspace, cardId: CardId, evaluationParameters: EvaluationParameters): LearnCard {
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspace.name) throw CardBindingException(workspace, cardId)
        val learnCard = learnCardRepository.findById(cardId.id).toNullable() ?: throw LearnCardNotFoundException(workspace, cardId)
        val inputValues = InputValues.create(evaluationParameters, learnCard)
        Sm2Algorithm.validate(inputValues)
        val outputValues = Sm2Algorithm.evaluate(inputValues)
        val updatedLearnCard = LearnCardEntity.create(cardId, workspace, outputValues, Instant.now())
        return LearnCard.create(learnCardRepository.save(updatedLearnCard))
    }

    fun getLearnCardsByWorkspace(workspace: Workspace): List<LearnCard> {
        return learnCardRepository.findByWorkspaceName(workspace.name).stream()
                .map { learnCardEntity -> LearnCard.create(learnCardEntity) }
                .toList()
    }

    fun delete(workspace: Workspace, cardId: CardId) {
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspace.name) throw CardBindingException(workspace, cardId)
        val learnCard = learnCardRepository.findById(cardId.id).toNullable() ?: throw LearnCardNotFoundException(workspace, cardId)
        learnCardRepository.delete(learnCard)
    }
}
