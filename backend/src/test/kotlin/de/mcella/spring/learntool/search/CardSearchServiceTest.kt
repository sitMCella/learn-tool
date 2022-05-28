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
    fun `given a Workspace request and a SearchPattern content, when searching the Cards and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val searchPattern = SearchPattern("content")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(false)

        cardSearchService.searchCards(workspaceRequest, searchPattern, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request and a SearchPattern content, when searching the Cards and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val searchPattern = SearchPattern("content")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.exists(workspaceRequest)).thenReturn(true)
        Mockito.`when`(workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        cardSearchService.searchCards(workspaceRequest, searchPattern, userPrincipal)
    }
}
