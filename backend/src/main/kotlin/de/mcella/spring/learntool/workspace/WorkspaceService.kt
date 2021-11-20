package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.streams.toList
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
        val workspaceEntity = WorkspaceEntity.create(workspace)
        return Workspace.create(workspaceRepository.save(workspaceEntity))
    }

    fun getAll(): List<Workspace> {
        return workspaceRepository.findAll()
                .stream()
                .map { workspaceEntity -> Workspace.create(workspaceEntity) }
                .toList()
    }

    fun exists(workspaceName: String): Boolean = workspaceRepository.existsById(workspaceName)
}
