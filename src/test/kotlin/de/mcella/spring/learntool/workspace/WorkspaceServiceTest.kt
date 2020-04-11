package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test
import org.mockito.Mockito

class WorkspaceServiceTest {

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val workspaceService = WorkspaceService(workspaceRepository)

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace with invalid name, when creating the Workspace, then throw InvalidWorkspaceNameException`() {
        val workspace = Workspace("workspaceTest!")
        Mockito.`when`(workspaceRepository.save(workspace)).thenReturn(workspace)

        workspaceService.create(workspace)
    }

    @Test(expected = WorkspaceAlreadyExistsException::class)
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace already exists, then throw WorkspaceAlreadyExistsException`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(workspaceRepository.save(workspace)).thenReturn(workspace)

        workspaceService.create(workspace)
    }

    @Test
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace does not already exist, then call the method save of WorkspaceRepository and return the Workspace`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)
        Mockito.`when`(workspaceRepository.save(workspace)).thenReturn(workspace)

        val createdWorkspace = workspaceService.create(workspace)

        Mockito.verify(workspaceRepository).save(workspace)
        assertEquals(workspace, createdWorkspace)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace exists, then call the method existsById of WorkspaceRepository and return true`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(true)

        val workspaceExists = workspaceService.exists(workspaceName)

        assertTrue(workspaceExists)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace does not exist, then call the method existsById of WorkspaceRepository and return false`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(workspaceRepository.existsById(workspaceName)).thenReturn(false)

        val workspaceExists = workspaceService.exists(workspaceName)

        assertFalse(workspaceExists)
    }
}
