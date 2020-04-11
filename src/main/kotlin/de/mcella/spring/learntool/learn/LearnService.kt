package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.common.toNullable
import de.mcella.spring.learntool.learn.algorithm.InputValues
import de.mcella.spring.learntool.learn.algorithm.Sm2Algorithm
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.CardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.CardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class LearnService(private val cardService: CardService, private val learnCardRepository: LearnCardRepository) {

    fun getCard(workspaceName: String): Card = cardService.getFirstCardFromWorkspace(workspaceName) ?: throw CardsNotFoundException(workspaceName)

    fun evaluateCard(workspaceName: String, evaluationParameters: EvaluationParameters): LearnCard {
        val cardId = evaluationParameters.cardId
        val card = cardService.findById(cardId) ?: throw CardNotFoundException(workspaceName, cardId)
        if (card.workspaceName != workspaceName) throw CardBindingException(workspaceName, cardId)
        val learnCard = learnCardRepository.findById(cardId).toNullable() ?: learnCardRepository.save(LearnCard.createInitial(cardId, workspaceName, Instant.now()))
        val inputValues = InputValues.create(evaluationParameters, learnCard)
        Sm2Algorithm.validate(inputValues)
        val outputValues = Sm2Algorithm.evaluate(inputValues)
        val updatedLearnCard = LearnCard.create(cardId, workspaceName, outputValues, Instant.now())
        return learnCardRepository.save(updatedLearnCard)
    }
}
