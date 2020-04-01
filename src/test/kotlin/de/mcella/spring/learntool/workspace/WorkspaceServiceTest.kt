package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import org.junit.Test
import org.mockito.Mockito

class WorkspaceServiceTest {

    private val workspaceRepository = Mockito.mock(WorkspaceRepository::class.java)

    private val workspaceService = WorkspaceService(workspaceRepository)

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace with invalid name, when creating the Workspace, then throw InvalidWorkspaceNameException`() {
        val workspace = Workspace("workspaceTest!")
        workspaceService.create(workspace)
    }

    @Test(expected = WorkspaceAlreadyExistsException::class)
    fun `given a Workspace with valid name and the Workspace already exists, when creating the Workspace, then throw WorkspaceAlreadyExistsException`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(true)

        workspaceService.create(workspace)
    }

    @Test
    fun `given a Workspace with valid name and the Workspace does not already exist, when creating the Workspace, then call the create method in WorkspaceRepository`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(workspaceRepository.existsById(workspace.name)).thenReturn(false)

        workspaceService.create(workspace)

        Mockito.verify(workspaceRepository).save(workspace)
    }
}
