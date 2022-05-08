package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.ByteArrayInputStream
import java.util.Collections
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Category(UnitTest::class)
class CardImportServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val cardImportService = CardImportService(cardService)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a Cards stream content, when creating the Cards, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent = CardContent("questionTest1", "responseTest1")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspaceRequest, cardContent, user)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        cardImportService.createMany(workspaceRequest, ByteArrayInputStream(streamContent.toByteArray()), user)
    }

    @Test(expected = CardAlreadyExistsException::class)
    fun `given a Workspace name and a Cards stream content, when creating the Cards and the Card already exists, then throw CardAlreadyExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent = CardContent("questionTest1", "responseTest1")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspaceRequest, cardContent, user)).thenThrow(CardAlreadyExistsException(CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")))

        cardImportService.createMany(workspaceRequest, ByteArrayInputStream(streamContent.toByteArray()), user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and a Cards stream content, when creating the Cards and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val cardContent = CardContent("questionTest1", "responseTest1")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspaceRequest, cardContent, user)).thenThrow(UserNotAuthorizedException(user))

        cardImportService.createMany(workspaceRequest, ByteArrayInputStream(streamContent.toByteArray()), user)
    }

    @Test
    fun `given a Workspace name and a Cards stream content, when creating the Cards, then call the method create of CardService`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        cardImportService.createMany(workspaceRequest, ByteArrayInputStream(streamContent.toByteArray()), user)

        val cardContent1 = CardContent("questionTest1", "responseTest1")
        Mockito.verify(cardService).create(workspaceRequest, cardContent1, user)
        val cardContent2 = CardContent("questionTest2", "responseTest2")
        Mockito.verify(cardService).create(workspaceRequest, cardContent2, user)
    }
}
