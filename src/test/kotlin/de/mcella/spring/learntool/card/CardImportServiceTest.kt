package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.ByteArrayInputStream
import org.junit.Test
import org.mockito.Mockito

class CardImportServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val cardImportService = CardImportService(cardService)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Cards stream content, when creating the Cards, then throw WorkspaceNotExistsException`() {
        val workspaceName = "workspaceTest"
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.`when`(cardService.create(workspaceName, cardContent1)).thenThrow(WorkspaceNotExistsException(workspaceName))

        cardImportService.createMany(workspaceName, ByteArrayInputStream(streamContent.toByteArray()))
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Cards stream content, when creating the Cards, then throw CardAlreadyExistsException if a Card id already exists`() {
        val workspaceName = "workspaceTest"
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.`when`(cardService.create(workspaceName, cardContent1)).thenThrow(CardAlreadyExistsException("9e493dc0-ef75-403f-b5d6-ed510634f8a6"))

        cardImportService.createMany(workspaceName, ByteArrayInputStream(streamContent.toByteArray()))
    }

    @Test
    fun `given a Workspace name and a Cards stream content, when creating the Cards, then call the method create of CardService`() {
        val workspaceName = "workspaceTest"
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"

        cardImportService.createMany(workspaceName, ByteArrayInputStream(streamContent.toByteArray()))

        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.verify(cardService).create(workspaceName, cardContent1)
        val cardContent2 = CardContent("questionTest2", "responseTest2")
        Mockito.verify(cardService).create(workspaceName, cardContent2)
    }
}
