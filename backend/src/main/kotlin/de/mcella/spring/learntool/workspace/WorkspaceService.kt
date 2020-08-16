package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import org.springframework.stereotype.Service

@Service
class WorkspaceService(private val workspaceRepository: WorkspaceRepository) {

    fun create(workspace: Workspace): Workspace {
        WorkspaceNameValidator.validate(workspace.name)
        if (workspaceRepository.existsById(workspace.name)) {
            throw WorkspaceAlreadyExistsException(
                workspace
            )
        }
        return workspaceRepository.save(workspace)
    }

    fun getAll(): List<Workspace> = workspaceRepository.findAll()

    fun exists(workspaceName: String): Boolean = workspaceRepository.existsById(workspaceName)
}
