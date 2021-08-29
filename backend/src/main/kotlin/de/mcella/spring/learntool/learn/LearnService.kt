package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.common.toNullable
import de.mcella.spring.learntool.learn.algorithm.InputValues
import de.mcella.spring.learntool.learn.algorithm.Sm2Algorithm
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.springframework.stereotype.Service

@Service
class LearnService(private val cardService: CardService, private val learnCardRepository: LearnCardRepository) {

    fun create(workspaceName: String, learnCardParameters: LearnCardParameters): LearnCard {
        val cardId = learnCardParameters.cardId
        if (learnCardRepository.existsById(cardId)) {
            throw LearnCardAlreadyExistsException(cardId)
        }
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspaceName) throw CardBindingException(workspaceName, cardId)
        return learnCardRepository.save(LearnCard.createInitial(cardId, workspaceName, Instant.now()))
    }

    fun getCard(workspaceName: String): Card {
        val today = LocalDate.now()
        val begin = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        val learnCard = learnCardRepository.findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end).toNullable() ?: throw LearnCardsNotFoundException(workspaceName)
        return cardService.findById(learnCard.id)
    }

    fun evaluateCard(workspaceName: String, evaluationParameters: EvaluationParameters): LearnCard {
        val cardId = evaluationParameters.cardId
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspaceName) throw CardBindingException(workspaceName, cardId)
        val learnCard = learnCardRepository.findById(cardId).toNullable() ?: throw LearnCardNotFoundException(workspaceName, cardId)
        val inputValues = InputValues.create(evaluationParameters, learnCard)
        Sm2Algorithm.validate(inputValues)
        val outputValues = Sm2Algorithm.evaluate(inputValues)
        val updatedLearnCard = LearnCard.create(cardId, workspaceName, outputValues, Instant.now())
        return learnCardRepository.save(updatedLearnCard)
    }

    fun delete(workspaceName: String, learnCardParameters: LearnCardParameters) {
        val cardId = learnCardParameters.cardId
        val card = cardService.findById(cardId)
        if (card.workspaceName != workspaceName) throw CardBindingException(workspaceName, cardId)
        val learnCard = learnCardRepository.findById(cardId).toNullable() ?: throw LearnCardNotFoundException(workspaceName, cardId)
        learnCardRepository.delete(learnCard)
    }
}
