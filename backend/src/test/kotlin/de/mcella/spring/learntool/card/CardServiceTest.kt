package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.dto.CardPagination
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceIdException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.util.Collections
import java.util.Optional
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Category(UnitTest::class)
class CardServiceTest {

    private val cardRepository = Mockito.mock(CardRepository::class.java)

    private val workspaceService = Mockito.mock(WorkspaceService::class.java)

    private val cardIdGenerator = Mockito.mock(CardIdGenerator::class.java)

    private val cardService = CardService(cardRepository, workspaceService, cardIdGenerator)

    @Test(expected = IllegalArgumentException::class)
    fun `given a Workspace request and a Card content with empty question, when creating the Card, then throw IllegalArgumentException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Workspace request and a Card content with empty response, when creating the Card, then throw IllegalArgumentException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Workspace request and a Card content, when creating the Card and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.create(workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request and a Card content, when creating the Card and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        cardService.create(workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace request and a Card content, when creating the Card and the Card already exists, then throw CardAlreadyExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(true)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)

        cardService.create(workspaceRequest, cardContent, userPrincipal)
    }

    @Test
    fun `given a Workspace request and a Card content, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(false)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.id, "question", "response"))

        val card = cardService.create(workspaceRequest, cardContent, userPrincipal)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(workspaceRequest.id, createdCard.workspaceId)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card.create(createdCard)
        assertEquals(expectedCard, card)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty id, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("", "workspaceId", "question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(card, userPrincipal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty workspace id, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "", "question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(card, userPrincipal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty question, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "workspaceId", "", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(card, userPrincipal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty response, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "workspaceId", "question", "")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(card, userPrincipal)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card, when creating the Card and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceRequest.id, "question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.create(card, userPrincipal)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Card, when creating the Card and the Card Id already exists, then throw CardAlreadyExistsException`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val card = Card(cardId.id, workspaceRequest.id, "question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(cardId.id)).thenReturn(true)

        cardService.create(card, userPrincipal)
    }

    @Test
    fun `given a Card, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val originalCard = Card(cardId.id, workspaceRequest.id, "question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(cardId.id)).thenReturn(false)
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.id, "question", "response"))

        val card = cardService.create(originalCard, userPrincipal)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(cardId.id, createdCard.id)
        assertEquals(workspaceRequest.id, createdCard.workspaceId)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card.create(createdCard)
        assertEquals(expectedCard, card)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id, a Workspace and a Card content, when updating the Card and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.update(cardId, workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Card Id, a Workspace and a Card content, when updating the Card and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        cardService.update(cardId, workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id, a Workspace and a Card content, when updating a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.update(cardId, workspaceRequest, cardContent, userPrincipal)
    }

    @Test(expected = InvalidWorkspaceIdException::class)
    fun `given a Card Id, a Workspace request and a Card content, when updating a Card and the Workspace Id does not match, then throw InvalidWorkspaceIdException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val wrongWorkspace = WorkspaceRequest("wrongWorkspaceId")
        Mockito.`when`(workspaceService.exists(wrongWorkspace)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(wrongWorkspace, userPrincipal)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.update(cardId, wrongWorkspace, cardContent, userPrincipal)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @Test
    fun `given a Card Id, a Workspace request and a Card content, when updating a Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.id, "updated question", "updated response"))
        val updatedCardContent = CardContent("updated question", "updated response")

        val savedCard = cardService.update(cardId, workspaceRequest, updatedCardContent, userPrincipal)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCardEntity = argumentCaptor.value
        assertEquals(cardId.id, createdCardEntity.id)
        assertEquals(workspaceRequest.id, createdCardEntity.workspaceId)
        assertEquals("updated question", createdCardEntity.question)
        assertEquals("updated response", createdCardEntity.response)
        val expectedCard = Card.create(createdCardEntity)
        assertEquals(expectedCard, savedCard)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id and a Workspace request, when deleting a Card and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.delete(cardId, workspaceRequest, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Card Id and a Workspace request, when deleting a Card and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        cardService.delete(cardId, workspaceRequest, userPrincipal)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id and a Workspace request, when deleting a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.delete(cardId, workspaceRequest, userPrincipal)
    }

    @Test(expected = InvalidWorkspaceIdException::class)
    fun `given a Card Id and a Workspace request, when deleting a Card and the Workspace Id does not match, then throw InvalidWorkspaceIdException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val wrongWorkspaceRequest = WorkspaceRequest("wrongWorkspaceId")
        Mockito.`when`(workspaceService.exists(wrongWorkspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(wrongWorkspaceRequest, userPrincipal)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, wrongWorkspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Card Id and a Workspace request, when deleting a Card, then call the method delete of CardRepository`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, workspaceRequest, userPrincipal)

        Mockito.verify(cardRepository).delete(cardEntity)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Workspace request, when retrieving the Cards by Workspace and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.findByWorkspace(workspaceRequest, cardPagination, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request, when retrieving the Cards by Workspace and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        cardService.findByWorkspace(workspaceRequest, cardPagination, userPrincipal)
    }

    @Test
    fun `given a Workspace request and a CardPagination, when retrieving the Cards by Workspace, then call the method findByWorkspaceIdOrderByCreationDateDesc of CardRepository and return the Cards`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val pageRequest = PageRequest.of(cardPagination.page, cardPagination.size)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, "workspaceId", "question", "response")
        val cardEntities = listOf(cardEntity)
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenReturn(true)
        Mockito.`when`(cardRepository.findByWorkspaceIdOrderByCreationDateDesc(workspaceRequest.id, pageRequest)).thenReturn(cardEntities)

        val cards = cardService.findByWorkspace(workspaceRequest, cardPagination, userPrincipal)

        Mockito.verify(cardRepository).findByWorkspaceIdOrderByCreationDateDesc(workspaceRequest.id, pageRequest)
        val expectedCard = Card(cardId.id, "workspaceId", "question", "response")
        val expectedCards = listOf(expectedCard)
        assertEquals(expectedCards, cards)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Workspace request, when retrieving the count of Cards by Workspace and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.countByWorkspace(workspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace request, when retrieving the count of Cards by Workspace, then call the method countByWorkspaceId of CardRepository and return the count of Cards`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.countByWorkspaceId(workspaceRequest.id)).thenReturn(1L)

        val cardsCount = cardService.countByWorkspace(workspaceRequest, userPrincipal)

        Mockito.verify(cardRepository).countByWorkspaceId(workspaceRequest.id)
        assertEquals(1L, cardsCount)
    }

    @Test
    fun `given a Card Id, when retrieving the Card, then call the method findById of CardRepository and return the Card`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, "workspaceId", "question", "response")
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        val card = cardService.findById(cardId)

        Mockito.verify(cardRepository).findById(cardId.id)
        val expectedCard = Card(cardId.id, "workspaceId", "question", "response")
        assertEquals(expectedCard, card)
    }
}
