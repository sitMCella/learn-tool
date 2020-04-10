package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceDoesNotExistException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class CardServiceTest {

    private val cardRepository = Mockito.mock(CardRepository::class.java)

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val cardService = CardService(cardRepository, workspaceRepository)

    @Test(expected = WorkspaceDoesNotExistException::class)
    fun `given a non existent Workspace name and a Card content, when creating the Card, then throw WorkspaceDoesNotExistException`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        cardService.create(workspaceName, cardContent)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Card content, when creating the Card, then throw CardAlreadyExistsException if the Card id already exists`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(true)

        cardService.create(workspaceName, cardContent)
    }

    @Test
    fun `given a Workspace name and a Card content, when creating the Card, then call the method save of CardRepository`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(false)

        cardService.create(workspaceName, cardContent)

        val argumentCaptor = ArgumentCaptor.forClass(Card::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val card = argumentCaptor.value
        assertEquals(workspaceName, card.workspaceName)
        assertEquals("question", card.question)
        assertEquals("response", card.response)
    }

    @Test(expected = WorkspaceDoesNotExistException::class)
    fun `given a non existent Workspace name, when retrieving the first Card from the Workspace, then throw WorkspaceDoesNotExistException`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        cardService.getFirstCardFromWorkspace(workspaceName)
    }

    @Test
    fun `given a Workspace name, when retrieving the first Card from the Workspace and no Cards exist into the Workspace, then return null`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        Mockito.`when`(cardRepository.findFirstByWorkspaceName(workspaceName)).thenReturn(Optional.empty())

        val card = cardService.getFirstCardFromWorkspace(workspaceName)

        assertNull(card)
    }

    @Test
    fun `given a Workspace name, when retrieving the first Card from the Workspace, then return the Card`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)
        val expectedCard = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardRepository.findFirstByWorkspaceName(workspaceName)).thenReturn(Optional.of(expectedCard))

        val card = cardService.getFirstCardFromWorkspace(workspaceName)

        assertEquals(expectedCard, card)
    }
}
