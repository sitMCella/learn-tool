package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.util.Collections
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Category(UnitTest::class)
class WorkspaceServiceTest {

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val workspaceService = WorkspaceService(workspaceRepository)

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace with invalid name, when creating the Workspace, then throw InvalidWorkspaceNameException`() {
        val userId = UserId(123L)
        val user = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspace-InvalidTest!")
        val workspaceEntity = WorkspaceEntity("workspace-InvalidTest!", userId.id)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        workspaceService.create(workspaceRequest, user)
    }

    @Test(expected = WorkspaceAlreadyExistsException::class)
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace already exists, then throw WorkspaceAlreadyExistsException`() {
        val user = UserPrincipal(123L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val workspaceEntity = WorkspaceEntity("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.name)).thenReturn(true)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        workspaceService.create(workspaceRequest, user)
    }

    @Test
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace does not already exist, then call the method save of WorkspaceRepository and return the Workspace`() {
        val userId = UserId(123L)
        val user = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val workspaceEntity = WorkspaceEntity("workspaceTest", userId.id)
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.name)).thenReturn(false)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        val createdWorkspace = workspaceService.create(workspaceRequest, user)

        Mockito.verify(workspaceRepository).save(workspaceEntity)
        val workspace = Workspace.create(workspaceRequest, user)
        assertEquals(workspace, createdWorkspace)
    }

    @Test
    fun `when retrieving all the Workspaces of the authenticated user, then call the method findAll of WorkspaceRepository and return the list of Workspaces`() {
        val userId = UserId(1L)
        val user = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceEntity1 = WorkspaceEntity("workspaceTest1", userId.id)
        val workspaceEntity2 = WorkspaceEntity("workspaceTest2", userId.id)
        val workspaceEntities = listOf(workspaceEntity1, workspaceEntity2)
        Mockito.`when`(workspaceRepository.findByUserId(userId.id)).thenReturn(workspaceEntities)

        val retrievedWorkspaces = workspaceService.getAll(user)

        Mockito.verify(workspaceRepository).findByUserId(userId.id)
        val workspaces = listOf(Workspace("workspaceTest1", userId), Workspace("workspaceTest2", userId))
        assertEquals(workspaces, retrievedWorkspaces)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace exists, then call the method existsById of WorkspaceRepository and return true`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.name)).thenReturn(true)

        val workspaceExists = workspaceService.exists(workspaceRequest)

        assertTrue(workspaceExists)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace does not exist, then call the method existsById of WorkspaceRepository and return false`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.name)).thenReturn(false)

        val workspaceExists = workspaceService.exists(workspaceRequest)

        assertFalse(workspaceExists)
    }
}
