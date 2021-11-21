package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.Workspace
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

    private val cardIdGenerator = Mockito.mock(CardIdGenerator::class.java)

    private val cardService = CardService(cardRepository, workspaceRepository, cardIdGenerator)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Card content, when creating the Card, then throw WorkspaceNotExistsException`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)

        cardService.create(workspace, cardContent)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Card content, when creating the Card and the Card already exists, then throw CardAlreadyExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(true)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)

        cardService.create(workspace, cardContent)
    }

    @Test
    fun `given a Workspace name and a Card content, when creating the Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(cardRepository.existsById(anyString())).thenReturn(false)
        Mockito.`when`(cardIdGenerator.create()).thenReturn(cardId)
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspace.name, "question", "response"))

        val card = cardService.create(workspace, cardContent)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCard = argumentCaptor.value
        assertEquals(workspace.name, createdCard.workspaceName)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card.create(createdCard)
        assertEquals(expectedCard, card)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id, a non existent Workspace name and a Card content, when updating the Card, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)

        cardService.update(cardId, workspace, cardContent)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id, a Workspace name and a Card content, when updating a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.update(cardId, workspace, cardContent)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id, a wrong Workspace name and a Card content, when updating a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        val wrongWorkspaceName = "wrongWorkspaceNameTest"
        val wrongWorkspace = Workspace(wrongWorkspaceName)
        Mockito.`when`(workspaceRepository.existsById(wrongWorkspaceName)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.update(cardId, wrongWorkspace, cardContent)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @Test
    fun `given a Card Id, a Workspace name and a Card content, when updating a Card, then call the method save of CardRepository and return the Card`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))
        Mockito.`when`(cardRepository.save(any(CardEntity::class.java))).thenReturn(CardEntity(cardId.id, workspace.name, "updated question", "updated response"))
        val updatedCardContent = CardContent("updated question", "updated response")

        val savedCard = cardService.update(cardId, workspace, updatedCardContent)

        val argumentCaptor = ArgumentCaptor.forClass(CardEntity::class.java)
        Mockito.verify(cardRepository).save(argumentCaptor.capture())
        val createdCardEntity = argumentCaptor.value
        assertEquals(cardId.id, createdCardEntity.id)
        assertEquals(workspace.name, createdCardEntity.workspaceName)
        assertEquals("updated question", createdCardEntity.question)
        assertEquals("updated response", createdCardEntity.response)
        val expectedCard = Card.create(createdCardEntity)
        assertEquals(expectedCard, savedCard)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Card Id and a non existent Workspace name, when deleting a Card, then throw WorkspaceNotExistsException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)

        cardService.delete(cardId, workspace)
    }

    @Test(expected = CardNotFoundException::class)
    fun `given a Card Id and a Workspace name, when deleting a non existent Card, then throw CardNotFoundException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.empty())

        cardService.delete(cardId, workspace)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Card Id and a wrong Workspace name, when deleting a Card, then throw InvalidWorkspaceNameException`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        val wrongWorkspace = Workspace("wrongWorkspaceNameTest")
        Mockito.`when`(workspaceRepository.existsById(wrongWorkspace.name)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, wrongWorkspace)
    }

    @Test
    fun `given a Card Id and a Workspace name, when deleting a Card, then call the method delete of CardRepository`() {
        val cardId = CardId("cardIdTest")
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        Mockito.`when`(cardRepository.findById(cardId.id)).thenReturn(Optional.of(cardEntity))

        cardService.delete(cardId, workspace)

        Mockito.verify(cardRepository).delete(cardEntity)
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
