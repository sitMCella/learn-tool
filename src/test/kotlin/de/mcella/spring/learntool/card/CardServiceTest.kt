package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceDoesNotExistException
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.test.assertEquals
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
}
