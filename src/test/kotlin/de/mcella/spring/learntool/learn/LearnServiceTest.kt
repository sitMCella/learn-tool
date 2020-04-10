package de.mcella.spring.learntool.learn

import com.nhaarman.mockitokotlin2.times
import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.CardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.CardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito

class LearnServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val learnCardRepository = Mockito.mock(LearnCardRepository::class.java)

    private val learnService = LearnService(cardService, learnCardRepository)

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method getFirstCardFromWorkspace of CardService and return the Card`() {
        val workspaceName = "workspaceTest"
        val expectedCard = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.getFirstCardFromWorkspace(workspaceName)).thenReturn(expectedCard)

        val card = learnService.getCard(workspaceName)

        Mockito.verify(cardService).getFirstCardFromWorkspace(workspaceName)
        assertEquals(expectedCard, card)
    }

    @Test(expected = CardsNotFoundException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and no Cards exist into the Workspace, then throw CardsNotFoundException`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(cardService.getFirstCardFromWorkspace(workspaceName)).thenReturn(null)

        learnService.getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method findById of CardService`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)

        Mockito.verify(cardService).findById(cardId)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method findById of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)

        Mockito.verify(learnCardRepository).findById(cardId)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the LearnCard does not exist, then call the method save of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.empty())
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCard::class.java)
        Mockito.verify(learnCardRepository, times(2)).save(argumentCaptor.capture())
        val updatedLearnCards = argumentCaptor.allValues
        assertEquals(LearnCard.createInitial(cardId, updatedLearnCards[0].lastReview), updatedLearnCards[0])
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method save of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCard::class.java)
        Mockito.verify(learnCardRepository).save(argumentCaptor.capture())
        val updatedLearnCard = argumentCaptor.value
        assertEquals(cardId, updatedLearnCard.id)
        assertTrue { updatedLearnCard.nextReview > learnCard.nextReview }
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the Card does not exist, then throw CardNotFoundException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        Mockito.`when`(cardService.findById(cardId)).thenReturn(null)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)
    }

    @Test(expected = CardBindingException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the Card does not exist into the Workspace, then throw CardBindingException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card(cardId, "anotherWorkspaceName", "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)
    }
}
