package de.mcella.spring.learntool.search

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.util.Collections
import javax.persistence.EntityManager
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Category(UnitTest::class)
class CardSearchServiceTest {

    private val entityManager = Mockito.mock(EntityManager::class.java)

    private val workspaceService = Mockito.mock(WorkspaceService::class.java)

    private val cardSearchService = CardSearchService(entityManager, workspaceService)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a SearchPattern content, when searching the Cards, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val searchPattern = SearchPattern("content")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenReturn(true)
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardSearchService.searchCards(workspaceRequest, searchPattern, user)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace name and a SearchPattern content, when searching the Cards and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val searchPattern = SearchPattern("content")
        val user = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, user)).thenThrow(UserNotAuthorizedException(user))
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)

        cardSearchService.searchCards(workspaceRequest, searchPattern, user)
    }
}
