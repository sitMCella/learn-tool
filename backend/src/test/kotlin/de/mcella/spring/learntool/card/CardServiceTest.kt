package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.util.Optional
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

@Category(UnitTest::class)
class CardServiceTest {

    private val cardRepository = Mockito.mock(CardRepository::class.java)

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val cardService = CardService(cardRepository, workspaceRepository)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Card content, when creating the Card, then throw WorkspaceNotExistsException`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        cardService.create(workspaceName, cardContent)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Card content, when creating the Card and the Card already exists, then throw CardAlreadyExistsException`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(true)

        cardService.create(workspaceName, cardContent)
    }

    @Test
    fun `given a Workspace name and a Card content, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(false)

        val card = cardService.create(workspaceName, cardContent)

        val argumentCaptor = ArgumentCaptor.forClass(Card::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(workspaceName, createdCard.workspaceName)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        assertEquals(createdCard, card)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id, a non existent Workspace name and a Card content, when updating the Card, then throw WorkspaceNotExistsException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        cardService.update(cardId, workspaceName, cardContent)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id, a Workspace name and a Card content, when updating a non existent Card, then throw CardNotFoundException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.empty())

        cardService.update(cardId, workspaceName, cardContent)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id, a wrong Workspace name and a Card content, when updating a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        val wrongWorkspaceName = "wrongWorkspaceNameTest"
        Mockito.`when`(workspaceRepository.existsById(wrongWorkspaceName)).thenReturn(true)
        val card = Card.create(cardId, workspaceName, cardContent)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.of(card))

        cardService.update(cardId, wrongWorkspaceName, cardContent)
    }

    @Test
    fun `given a Card Id, a Workspace name and a Card content, when updating a Card, then call the method save of CardRepository and return the Card`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        val card = Card.create(cardId, workspaceName, cardContent)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.of(card))
        val updatedCardContent = CardContent("updated question", "updated response")

        val savedCard = cardService.update(cardId, workspaceName, updatedCardContent)

        val argumentCaptor = ArgumentCaptor.forClass(Card::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(cardId, createdCard.id)
        assertEquals(workspaceName, createdCard.workspaceName)
        assertEquals("updated question", createdCard.question)
        assertEquals("updated response", createdCard.response)
        assertEquals(createdCard, savedCard)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id and a non existent Workspace name, when deleting a Card, then throw WorkspaceNotExistsException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        cardService.delete(cardId, workspaceName)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id and a Workspace name, when deleting a non existent Card, then throw CardNotFoundException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.empty())

        cardService.delete(cardId, workspaceName)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id and a wrong Workspace name, when deleting a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        val wrongWorkspaceName = "wrongWorkspaceNameTest"
        Mockito.`when`(workspaceRepository.existsById(wrongWorkspaceName)).thenReturn(true)
        val card = Card.create(cardId, workspaceName, cardContent)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.of(card))

        cardService.delete(cardId, wrongWorkspaceName)
    }

    @Test
    fun `given a Card Id and a Workspace name, when deleting a Card, then call the method delete of CardRepository`() {
        val cardId = "cardIdTest"
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        val card = Card.create(cardId, workspaceName, cardContent)
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.of(card))

        cardService.delete(cardId, workspaceName)

        Mockito.verify(cardRepository).delete(card)
    }

    @Test
    fun `given a Card id, when retrieving the Card, then call the method findById of CardRepository and return the Card`() {
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val expectedCard = Card(cardId, "workspaceTest", "question", "response")
        Mockito.`when`(cardRepository.findById(cardId)).thenReturn(Optional.of(expectedCard))

        val card = cardService.findById(cardId)

        Mockito.verify(cardRepository).findById(cardId)
        assertEquals(expectedCard, card)
    }
}
