package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.user.exceptions.UserNotExistentException
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.dto.WorkspaceId
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.util.Optional
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

    private val workspaceIdGenerator = Mockito.mock(WorkspaceIdGenerator::class.java)

    private val workspaceService = WorkspaceService(workspaceRepository, workspaceIdGenerator)

    @Test(expected = IllegalArgumentException::class)
    fun `given a Workspace create request, when creating the Workspace and the Workspace name is empty, then throw IllegalArgumentException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceCreateRequest = WorkspaceCreateRequest("")

        workspaceService.create(workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace create request, when creating the Workspace and the Workspace has an invalid name, then throw InvalidWorkspaceNameException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace-InvalidName!")

        workspaceService.create(workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = WorkspaceAlreadyExistsException::class)
    fun `given a Workspace create request, when creating the Workspace and the Workspace Id already exists, then throw WorkspaceAlreadyExistsException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        val workspaceId = WorkspaceId("workspaceId")
        Mockito.`when`(workspaceIdGenerator.create()).thenReturn(workspaceId)
        Mockito.`when`(workspaceRepository.existsById(workspaceId.id)).thenReturn(true)

        workspaceService.create(workspaceCreateRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace create request, when creating the Workspace and the Workspace does not already exist, then call the method save of WorkspaceRepository and return the Workspace`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        val workspaceId = WorkspaceId("workspaceId")
        Mockito.`when`(workspaceIdGenerator.create()).thenReturn(workspaceId)
        val workspaceEntity = WorkspaceEntity(workspaceId.id, workspaceCreateRequest.name, userPrincipal.id!!)
        Mockito.`when`(workspaceRepository.existsById(workspaceId.id)).thenReturn(false)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        val createdWorkspace = workspaceService.create(workspaceCreateRequest, userPrincipal)

        Mockito.verify(workspaceRepository).save(workspaceEntity)
        val workspace = Workspace.create(workspaceId, workspaceCreateRequest, userPrincipal)
        assertEquals(workspace, createdWorkspace)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a Workspace create request, when updating the Workspace and the Workspace name is empty, then throw IllegalArgumentException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("")

        workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace create request, when updating the Workspace and the Workspace has an invalid name, then throw InvalidWorkspaceNameException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace-InvalidName!")

        workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Workspace create request, when updating the Workspace and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(false)

        workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = UserNotExistentException::class)
    fun `given a Workspace request and a UserPrincipal, when updating the Workspace and the User does not exist, then throw UserNotExistentException`() {
        val userPrincipal = UserPrincipal(null, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)

        workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request and a UserPrincipal, when updating the Workspace and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Updated Name")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)
        val workspaceEntity = WorkspaceEntity(workspaceRequest.id, "Workspace Name", 2L)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace create request, when updating the Workspace, then call the method save of WorkspaceRepository and return the Workspace`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Updated Name")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)
        val originalWorkspaceEntity = WorkspaceEntity(workspaceRequest.id, "Workspace Name", userPrincipal.id!!)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(originalWorkspaceEntity))
        val workspaceEntity = WorkspaceEntity(workspaceRequest.id, workspaceCreateRequest.name, userPrincipal.id!!)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        val updatedWorkspace = workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)

        Mockito.verify(workspaceRepository).save(workspaceEntity)
        val workspace = Workspace.create(workspaceRequest, workspaceCreateRequest, userPrincipal)
        assertEquals(workspace, updatedWorkspace)
    }

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a Workspace request, when deleting a Workspace and the Workspace does not exist, then throw WorkspaceNotExistsException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.empty())

        workspaceService.delete(workspaceRequest, userPrincipal)
    }

    @Test(expected = UserNotExistentException::class)
    fun `given a Workspace request and a UserPrincipal, when deleting the Workspace and the User does not exist, then throw UserNotExistentException`() {
        val userPrincipal = UserPrincipal(null, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceEntity = WorkspaceEntity(workspaceRequest.id, "Workspace Name", 1L)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.delete(workspaceRequest, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request and a UserPrincipal, when deleting the Workspace and the User does not own the Workspace, then throw UserNotAuthorizedException`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)
        val workspaceEntity = WorkspaceEntity(workspaceRequest.id, "Workspace Name", 2L)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.delete(workspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace request, when deleting the Workspace, then call the method delete of WorkspaceRepository`() {
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceRequest = WorkspaceRequest("workspaceId")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)
        val workspaceEntity = WorkspaceEntity(workspaceRequest.id, "Workspace Name", userPrincipal.id!!)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.delete(workspaceRequest, userPrincipal)

        Mockito.verify(workspaceRepository).delete(workspaceEntity)
    }

    @Test
    fun `when retrieving all the Workspaces of the authenticated User, then call the method findAll of WorkspaceRepository and return the list of Workspaces`() {
        val userId = UserId(1L)
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceEntity1 = WorkspaceEntity("workspaceId1", "Workspace Name 1", userId.id)
        val workspaceEntity2 = WorkspaceEntity("workspaceId2", "Workspace Name 2", userId.id)
        val workspaceEntities = listOf(workspaceEntity1, workspaceEntity2)
        Mockito.`when`(workspaceRepository.findByUserId(userId.id)).thenReturn(workspaceEntities)

        val retrievedWorkspaces = workspaceService.getAll(userPrincipal)

        Mockito.verify(workspaceRepository).findByUserId(userId.id)
        val workspaces = listOf(Workspace("workspaceId1", "Workspace Name 1", userId), Workspace("workspaceId2", "Workspace Name 2", userId))
        assertEquals(workspaces, retrievedWorkspaces)
    }

    @Test
    fun `given a Workspace request, when checking the Workspace existence and the Workspace exists, then call the method existsById of WorkspaceRepository and return true`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(true)

        val workspaceExists = workspaceService.exists(workspaceRequest)

        assertTrue(workspaceExists)
    }

    @Test
    fun `given a Workspace request, when checking the Workspace existence and the Workspace does not exist, then call the method existsById of WorkspaceRepository and return false`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        Mockito.`when`(workspaceRepository.existsById(workspaceRequest.id)).thenReturn(false)

        val workspaceExists = workspaceService.exists(workspaceRequest)

        assertFalse(workspaceExists)
    }

    @Test
    fun `given a Workspace request and a UserPrincipal, when verifying the User ownership of the Workspace and the Workspace does not exist, then return true`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.empty())

        val isAuthorized = workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)

        assertTrue(isAuthorized)
    }

    @Test(expected = UserNotExistentException::class)
    fun `given a Workspace request and a UserPrincipal, when verifying the User ownership of the Workspace and the user does not exist, then throw UserNotExistentException`() {
        val workspaceId = WorkspaceId("workspaceId")
        val workspaceRequest = WorkspaceRequest(workspaceId.id)
        val userPrincipal = UserPrincipal(null, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val userId = UserId(1L)
        val workspaceEntity = WorkspaceEntity(workspaceId.id, "Workspace Name", userId.id)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
    }

    @Test(expected = UserNotAuthorizedException::class)
    fun `given a Workspace request and a UserPrincipal, when verifying the User ownership of the Workspace and the user does not own the Workspace, then throw UserNotAuthorizedException`() {
        val workspaceId = WorkspaceId("workspaceId")
        val workspaceRequest = WorkspaceRequest(workspaceId.id)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val anotherUserId = UserId(2L)
        val workspaceEntity = WorkspaceEntity(workspaceId.id, "Workspace Name", anotherUserId.id)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace request and a UserPrincipal, when verifying the User ownership of the Workspace and the user owns the Workspace, then return true`() {
        val workspaceId = WorkspaceId("workspaceId")
        val workspaceRequest = WorkspaceRequest(workspaceId.id)
        val userId = UserId(1L)
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspaceEntity = WorkspaceEntity(workspaceId.id, "Workspace Name", userId.id)
        Mockito.`when`(workspaceRepository.findById(workspaceRequest.id)).thenReturn(Optional.of(workspaceEntity))

        val isAuthorized = workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)

        assertTrue(isAuthorized)
    }
}
