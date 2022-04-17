package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.ByteArrayInputStream
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito

@Category(UnitTest::class)
class CardImportServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val cardImportService = CardImportService(cardService)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Cards stream content, when creating the Cards, then throw WorkspaceNotExistsException`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.`when`(cardService.create(workspace, cardContent1)).thenThrow(WorkspaceNotExistsException(workspace))

        cardImportService.createMany(workspace, ByteArrayInputStream(streamContent.toByteArray()))
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Cards stream content, when creating the Cards and the Card already exists, then throw CardAlreadyExistsException`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.`when`(cardService.create(workspace, cardContent1)).thenThrow(CardAlreadyExistsException(CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")))

        cardImportService.createMany(workspace, ByteArrayInputStream(streamContent.toByteArray()))
    }

    @Test
    fun `given a Workspace name and a Cards stream content, when creating the Cards, then call the method create of CardService`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"

        cardImportService.createMany(workspace, ByteArrayInputStream(streamContent.toByteArray()))

        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.verify(cardService).create(workspace, cardContent1)
        val cardContent2 = CardContent("questionTest2", "responseTest2")
        Mockito.verify(cardService).create(workspace, cardContent2)
    }
}
