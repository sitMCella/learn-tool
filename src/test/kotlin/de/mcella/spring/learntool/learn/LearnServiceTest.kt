package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
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

    @Test(expected = LearnCardAlreadyExistsException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the LearnCard already exists, then throw LearnCardAlreadyExistsException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        Mockito.`when`(learnCardRepository.existsById(cardId)).thenReturn(true)
        val learnCardParameters = LearnCardParameters(cardId)

        learnService.create(workspaceName, learnCardParameters)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the Card does not exist, then throw CardNotFoundException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        Mockito.`when`(learnCardRepository.existsById(cardId)).thenReturn(false)
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))
        val learnCardParameters = LearnCardParameters(cardId)

        learnService.create(workspaceName, learnCardParameters)
    }

    @Test(expected = CardBindingException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the Card belongs to a different Workspace, then throw CardBindingException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        Mockito.`when`(learnCardRepository.existsById(cardId)).thenReturn(false)
        val card = Card(cardId, "anotherWorkspaceTest", "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)
        val learnCardParameters = LearnCardParameters(cardId)

        learnService.create(workspaceName, learnCardParameters)
    }

    @Test
    fun `given a Workspace name and a Card id, when creating a LearnCard, then call the method save of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        Mockito.`when`(learnCardRepository.existsById(cardId)).thenReturn(false)
        val card = Card(cardId, workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)
        val learnCardParameters = LearnCardParameters(cardId)

        learnService.create(workspaceName, learnCardParameters)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCard::class.java)
        Mockito.verify(learnCardRepository).save(argumentCaptor.capture())
        val createdLearnCard = argumentCaptor.value
        assertEquals(LearnCard.createInitial(cardId, workspaceName, createdLearnCard.lastReview), createdLearnCard)
    }

    @Test
    fun `given a Workspace name and a Card id, when creating a LearnCard, then return the LearnCard`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        Mockito.`when`(learnCardRepository.existsById(cardId)).thenReturn(false)
        val card = Card(cardId, workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val expectedLearnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(expectedLearnCard)
        val learnCardParameters = LearnCardParameters(cardId)

        val learnCard = learnService.create(workspaceName, learnCardParameters)

        assertEquals(expectedLearnCard, learnCard)
    }

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val expectedCard = Card(cardId, workspaceName, "question", "response")
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        val today = LocalDate.now()
        val begin = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(cardService.findById(cardId)).thenReturn(expectedCard)

        learnService.getCard(workspaceName)

        Mockito.verify(learnCardRepository).findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end)
    }

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method findById of CardService and return the Card`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val expectedCard = Card(cardId, workspaceName, "question", "response")
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        val today = LocalDate.now()
        val begin = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(cardService.findById(cardId)).thenReturn(expectedCard)

        val card = learnService.getCard(workspaceName)

        Mockito.verify(cardService).findById(cardId)
        assertEquals(expectedCard, card)
    }

    @Test(expected = LearnCardsNotFoundException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and no LearnCards for the given Workspace name exist, then throw LearnCardsNotFoundException`() {
        val workspaceName = "workspaceTest"
        val today = LocalDate.now()
        val begin = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end)).thenReturn(Optional.empty())

        learnService.getCard(workspaceName)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and the Card does not exist into the Workspace, then throw CardNotFoundException`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        val today = LocalDate.now()
        val begin = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName, begin, end)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))

        learnService.getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method findById of CardService`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
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
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)

        Mockito.verify(learnCardRepository).findById(cardId)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method save of LearnCardRepository`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
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
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
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
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCard::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceName, evaluationParameters)
    }
}
