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
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
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
    fun `given a CardContent with empty question, when creating the Card, then throw IllegalArgumentException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(workspaceRequest, cardContent, user)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a CardContent with empty response, when creating the Card, then throw IllegalArgumentException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardService.create(workspaceRequest, cardContent, user)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Card content, when creating the Card, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.create(workspaceRequest, cardContent, user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and a Card content, when creating the Card and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        cardService.create(workspaceRequest, cardContent, user)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Card content, when creating the Card and the Card already exists, then throw CardAlreadyExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(true)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)

        cardService.create(workspaceRequest, cardContent, user)
    }

    @Test
    fun `given a Workspace name and a Card content, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(false)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.name, "question", "response"))

        val card = cardService.create(workspaceRequest, cardContent, user)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(workspaceRequest.name, createdCard.workspaceName)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card.create(createdCard)
        assertEquals(expectedCard, card)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty id, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("", "workspaceTest", "question", "response")

        cardService.create(card)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty workspaceName, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "", "question", "response")

        cardService.create(card)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty question, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "workspaceTest", "", "response")

        cardService.create(card)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Card with empty response, when creating the Card, then throw IllegalArgumentException`() {
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", "workspaceTest", "question", "")

        cardService.create(card)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card with a non existent Workspace name, when creating the Card, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceRequest.name, "question", "response")
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.create(card)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Card with an already existent CardId, when creating the Card, then throw CardAlreadyExistsException`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val card = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(cardId.id)).thenReturn(true)

        cardService.create(card)
    }

    @Test
    fun `given a Card, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val originalCard = Card(cardId.id, workspaceRequest.name, "question", "response")
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(cardId.id)).thenReturn(false)
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.name, "question", "response"))

        val card = cardService.create(originalCard)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(cardId.id, createdCard.id)
        assertEquals(workspaceRequest.name, createdCard.workspaceName)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card.create(createdCard)
        assertEquals(expectedCard, card)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id, a non existent Workspace name and a Card content, when updating the Card, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.update(cardId, workspaceRequest, cardContent, user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Card Id, a Workspace name and a Card content, when updating the Card and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        cardService.update(cardId, workspaceRequest, cardContent, user)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id, a Workspace name and a Card content, when updating a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.update(cardId, workspaceRequest, cardContent, user)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id, a wrong Workspace name and a Card content, when updating a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val wrongWorkspaceName = "wrongWorkspaceNameTest"
        val wrongWorkspace = WorkspaceRequest(wrongWorkspaceName)
        Mockito.`when`(workspaceService.exists(wrongWorkspace)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(wrongWorkspace, user)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.update(cardId, wrongWorkspace, cardContent, user)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @Test
    fun `given a Card Id, a Workspace name and a Card content, when updating a Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspaceRequest.name, "updated question", "updated response"))
        val updatedCardContent = CardContent("updated question", "updated response")

        val savedCard = cardService.update(cardId, workspaceRequest, updatedCardContent, user)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCardEntity = argumentCaptor.value
        assertEquals(cardId.id, createdCardEntity.id)
        assertEquals(workspaceRequest.name, createdCardEntity.workspaceName)
        assertEquals("updated question", createdCardEntity.question)
        assertEquals("updated response", createdCardEntity.response)
        val expectedCard = Card.create(createdCardEntity)
        assertEquals(expectedCard, savedCard)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id and a non existent Workspace name, when deleting a Card, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.delete(cardId, workspaceRequest, user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Card Id and a Workspace name, when deleting a Card and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        cardService.delete(cardId, workspaceRequest, user)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id and a Workspace name, when deleting a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.delete(cardId, workspaceRequest, user)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id and a wrong Workspace name, when deleting a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val wrongWorkspace = WorkspaceRequest("wrongWorkspaceNameTest")
        Mockito.`when`(workspaceService.exists(wrongWorkspace)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(wrongWorkspace, user)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, wrongWorkspace, user)
    }

    @Test
    fun `given a Card Id and a Workspace name, when deleting a Card, then call the method delete of CardRepository`() {
        val cardId = CardId("cardIdTest")
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardContent = CardContent("question", "response")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspaceRequest, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, workspaceRequest, user)

        Mockito.verify(cardRepository).delete(cardEntity)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name, when retrieving the Cards by Workspace, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardPagination = CardPagination(0, 20)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.findByWorkspace(workspaceRequest, cardPagination, user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name, when retrieving the Cards by Workspace and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardPagination = CardPagination(0, 20)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))

        cardService.findByWorkspace(workspaceRequest, cardPagination, user)
    }

    @Test
    fun `given a Workspace name and a CardPagination, when retrieving the Cards by Workspace, then call the method findByWorkspaceNameDesc of CardRepository and return the Cards`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardPagination = CardPagination(0, 20)
        val pageRequest = PageRequest.of(cardPagination.page, cardPagination.size)
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, "workspaceTest", "question", "response")
        val cardEntities = listOf(cardEntity)
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(cardRepository.findByWorkspaceNameOrderByCreationDateDesc(workspaceRequest.name, pageRequest)).thenReturn(cardEntities)

        val cards = cardService.findByWorkspace(workspaceRequest, cardPagination, user)

        Mockito.verify(cardRepository).findByWorkspaceNameOrderByCreationDateDesc(workspaceRequest.name, pageRequest)
        val expectedCard = Card(cardId.id, "workspaceTest", "question", "response")
        val expectedCards = listOf(expectedCard)
        assertEquals(expectedCards, cards)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name, when retrieving the count of Cards by Workspace, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardService.countByWorkspace(workspaceRequest)
    }

    @Test
    fun `given a Workspace name, when retrieving the count of Cards by Workspace, then call the method countByWorkspaceName of CardRepository and return the count of Cards`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(cardRepository.countByWorkspaceName(workspaceRequest.name)).thenReturn(1L)

        val cardsCount = cardService.countByWorkspace(workspaceRequest)

        Mockito.verify(cardRepository).countByWorkspaceName(workspaceRequest.name)
        assertEquals(1L, cardsCount)
    }

    @Test
    fun `given a Card id, when retrieving the Card, then call the method findById of CardRepository and return the Card`() {
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, "workspaceTest", "question", "response")
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        val card = cardService.findById(cardId)

        Mockito.verify(cardRepository).findById(cardId.id)
        val expectedCard = Card(cardId.id, "workspaceTest", "question", "response")
        assertEquals(expectedCard, card)
    }
}
