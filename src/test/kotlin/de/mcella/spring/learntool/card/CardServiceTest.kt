package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.util.Optional
import kotlin.test.assertEquals
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

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
