package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito

@Category(UnitTest::class)
class WorkspaceServiceTest {

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val workspaceService = WorkspaceService(workspaceRepository)

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace with invalid name, when creating the Workspace, then throw InvalidWorkspaceNameException`() {
        val workspace = Workspace("workspace-InvalidTest!")
        val workspaceEntity = WorkspaceEntity("workspace-InvalidTest!")
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        workspaceService.create(workspace)
    }

    @Test(expected = WorkspaceAlreadyExistsException::class)
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace already exists, then throw WorkspaceAlreadyExistsException`() {
        val workspace = Workspace("workspaceTest")
        val workspaceEntity = WorkspaceEntity("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        workspaceService.create(workspace)
    }

    @Test
    fun `given a Workspace with valid name, when creating the Workspace and the Workspace does not already exist, then call the method save of WorkspaceRepository and return the Workspace`() {
        val workspace = Workspace("workspaceTest")
        val workspaceEntity = WorkspaceEntity("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)
        Mockito.`when`(workspaceRepository.save(workspaceEntity)).thenReturn(workspaceEntity)

        val createdWorkspace = workspaceService.create(workspace)

        Mockito.verify(workspaceRepository).save(workspaceEntity)
        assertEquals(workspace, createdWorkspace)
    }

    @Test
    fun `when retrieving all the Workspaces, then call the method findAll of WorkspaceRepository and return the list of Workspaces`() {
        val workspaceEntity1 = WorkspaceEntity("workspaceTest1")
        val workspaceEntity2 = WorkspaceEntity("workspaceTest2")
        val workspaceEntities = listOf(workspaceEntity1, workspaceEntity2)
        Mockito.`when`(workspaceRepository.findAll()).thenReturn(workspaceEntities)

        val retrievedWorkspaces = workspaceService.getAll()

        Mockito.verify(workspaceRepository).findAll()
        val workspaces = listOf(Workspace("workspaceTest1"), Workspace("workspaceTest2"))
        assertEquals(workspaces, retrievedWorkspaces)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace exists, then call the method existsById of WorkspaceRepository and return true`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)

        val workspaceExists = workspaceService.exists(workspace)

        assertTrue(workspaceExists)
    }

    @Test
    fun `given a Workspace name, when checking the Workspace existence and the Workspace does not exist, then call the method existsById of WorkspaceRepository and return false`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)

        val workspaceExists = workspaceService.exists(workspace)

        assertFalse(workspaceExists)
    }
}
