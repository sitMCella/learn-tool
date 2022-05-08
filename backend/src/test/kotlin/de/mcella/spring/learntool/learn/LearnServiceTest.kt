package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Collections
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Category(UnitTest::class)
class LearnServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val workspaceService = Mockito.mock(WorkspaceService::class.java)

    private val learnCardRepository = Mockito.mock(LearnCardRepository::class.java)

    private val learnService = LearnService(cardService, workspaceService, learnCardRepository)

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        learnService.create(workspaceRequest, cardId, user)
    }

    @Test(expected = LearnCardAlreadyExistsException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the LearnCard already exists, then throw LearnCardAlreadyExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(true)

        learnService.create(workspaceRequest, cardId, user)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the Card does not exist, then throw CardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(false)
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))

        learnService.create(workspaceRequest, cardId, user)
    }

    @Test(expected = CardBindingException::class)
    fun `given a Workspace name and a Card id, when creating a LearnCard and the Card belongs to a different Workspace, then throw CardBindingException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(false)
        val card = Card(cardId.id, "anotherWorkspaceTest", "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCardEntity)

        learnService.create(workspaceRequest, cardId, user)
    }

    @Test
    fun `given a Workspace name and a Card id, when creating a LearnCard, then call the method save of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(false)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCardEntity)

        learnService.create(workspaceRequest, cardId, user)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCardEntity::class.java)
        Mockito.verify(learnCardRepository).save(argumentCaptor.capture())
        val createdLearnCardEntity = argumentCaptor.value
        assertEquals(LearnCardEntity.createInitial(cardId, workspaceRequest, createdLearnCardEntity.lastReview), createdLearnCardEntity)
    }

    @Test
    fun `given a Workspace name and a Card id, when creating a LearnCard, then return the LearnCard`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(false)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCardEntity)

        val learnCard = learnService.create(workspaceRequest, cardId, user)

        val expectedLearnCard = LearnCard(cardId.id, workspaceRequest.name, learnCardEntity.lastReview, learnCardEntity.nextReview, learnCardEntity.repetitions, learnCardEntity.easeFactor, learnCardEntity.intervalDays)
        assertEquals(expectedLearnCard, learnCard)
    }

    @Test(expected = LearnCardAlreadyExistsException::class)
    fun `given a LearnCard, when creating a LearnCard and the LearnCard already exists, then throw LearnCardAlreadyExistsException`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(true)
        val today = Instant.now()
        val end = LocalDate.now().plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        val learnCard = LearnCard(cardId.id, "workspaceTest", today, end, 0, 1.3f, 0)

        learnService.create(learnCard)
    }

    @Test
    fun `given a LearnCard, when creating a LearnCard, then call the method save of LearnCardRepository and return the LearnCard`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(learnCardRepository.existsById(cardId.id)).thenReturn(false)
        val today = Instant.now()
        val end = LocalDate.now().plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        val originalLearnCard = LearnCard(cardId.id, workspaceRequest.name, today, end, 0, 1.3f, 0)
        val learnCardEntity = LearnCardEntity(cardId.id, workspaceRequest.name, today, end, 0, 1.3f, 0)
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCardEntity)

        val learnCard = learnService.create(originalLearnCard)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCardEntity::class.java)
        Mockito.verify(learnCardRepository).save(argumentCaptor.capture())
        val createdLearnCardEntity = argumentCaptor.value
        assertEquals(LearnCardEntity(cardId.id, workspaceRequest.name, today, end, 0, 1.3f, 0), createdLearnCardEntity)
        assertEquals(originalLearnCard, learnCard)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        learnService.getCard(workspaceRequest, user)
    }

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val expectedCard = Card(cardId.id, workspaceRequest.name, "question", "response")
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceRequest.name, end)).thenReturn(Optional.of(learnCardEntity))
        Mockito.`when`(cardService.findById(cardId)).thenReturn(expectedCard)

        learnService.getCard(workspaceRequest, user)

        Mockito.verify(learnCardRepository).findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceRequest.name, end)
    }

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method findById of CardService and return the Card`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val expectedCard = Card(cardId.id, workspaceRequest.name, "question", "response")
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceRequest.name, end)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(cardService.findById(cardId)).thenReturn(expectedCard)

        val card = learnService.getCard(workspaceRequest, user)

        Mockito.verify(cardService).findById(cardId)
        assertEquals(expectedCard, card)
    }

    @Test(expected = LearnCardsNotFoundException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and no LearnCards for the given Workspace name exist, then throw LearnCardsNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceRequest.name, end)).thenReturn(Optional.empty())

        learnService.getCard(workspaceRequest, user)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name, when retrieving a Card from the Workspace and the Card does not exist into the Workspace, then throw CardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val today = LocalDate.now()
        val end = today.plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
        Mockito.`when`(learnCardRepository.findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceRequest.name, end)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))

        learnService.getCard(workspaceRequest, user)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method findById of CardService`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)

        Mockito.verify(cardService).findById(cardId)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method findById of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)

        Mockito.verify(learnCardRepository).findById(cardId.id)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card, then call the method save of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCardEntity::class.java)
        Mockito.verify(learnCardRepository).save(argumentCaptor.capture())
        val updatedLearnCardEntity = argumentCaptor.value
        assertEquals(cardId, CardId(updatedLearnCardEntity.id))
        assertTrue { updatedLearnCardEntity.nextReview > learnCard.nextReview }
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the Card does not exist, then throw CardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)
    }

    @Test(expected = CardBindingException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the Card does not exist into the Workspace, then throw CardBindingException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, "anotherWorkspaceName", "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))
        Mockito.`when`(learnCardRepository.save(any(LearnCardEntity::class.java))).thenReturn(learnCard)

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)
    }

    @Test(expected = LearnCardNotFoundException::class)
    fun `given a Workspace name and the evaluation parameters, when evaluating a Card and the LearnCard does not exist, then throw LearnCardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, user)
    }

    @Test
    fun `given a Workspace name, when retrieving the LearnCards, then call the method findByWorkspaceName of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val learnCardEntities = listOf(learnCardEntity)
        Mockito.`when`(learnCardRepository.findByWorkspaceName(workspaceRequest.name)).thenReturn(learnCardEntities)

        val learnCards = learnService.getLearnCardsByWorkspace(workspaceRequest)

        Mockito.verify(learnCardRepository).findByWorkspaceName(workspaceRequest.name)
        val expectedLearnCard = LearnCard(learnCardEntity.id, learnCardEntity.workspaceName, learnCardEntity.lastReview, learnCardEntity.nextReview, learnCardEntity.repetitions, learnCardEntity.easeFactor, learnCardEntity.intervalDays)
        val expectedLearnCards = listOf(expectedLearnCard)
        assertEquals(expectedLearnCards, learnCards)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and a Card id, when deleting a LearnCard and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        learnService.delete(workspaceRequest, cardId, user)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Workspace name and a Card id, when deleting a LearnCard and the Card does not exist, then throw CardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardService.findById(cardId)).thenThrow(CardNotFoundException(cardId))

        learnService.delete(workspaceRequest, cardId, user)
    }

    @Test(expected = CardBindingException::class)
    fun `given a Workspace name and a Card id, when deleting a LearnCard and the Card belongs to a different Workspace, then throw CardBindingException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, "anotherWorkspaceTest", "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))

        learnService.delete(workspaceRequest, cardId, user)
    }

    @Test(expected = LearnCardNotFoundException::class)
    fun `given a Workspace name and a Card id, when deleting a LearnCard and the LearnCard does not exist, then throw LearnCardNotFoundException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        learnService.delete(workspaceRequest, cardId, user)
    }

    @Test
    fun `given a Workspace name and a Card id, when deleting a LearnCard, then call the method delete of LearnCardRepository`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(cardService.findById(cardId)).thenReturn(card)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        Mockito.`when`(learnCardRepository.findById(cardId.id)).thenReturn(Optional.of(learnCard))

        learnService.delete(workspaceRequest, cardId, user)

        val argumentCaptor = ArgumentCaptor.forClass(LearnCardEntity::class.java)
        Mockito.verify(learnCardRepository).delete(argumentCaptor.capture())
        val deletedLearnCard = argumentCaptor.value
        assertEquals(learnCard, deletedLearnCard)
    }
}
