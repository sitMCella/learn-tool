package de.mcella.spring.learntool.workspace

import org.springframework.stereotype.Service

@Service
class WorkspaceService(private val workspaceRepository: WorkspaceRepository) {

    fun create(workspace: Workspace) {
        WorkspaceNameValidator.validate(workspace.name)
        workspaceRepository.save(workspace)
    }
}
