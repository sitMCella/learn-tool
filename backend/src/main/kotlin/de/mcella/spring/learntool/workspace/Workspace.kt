package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Workspace(
    @field:NotNull @field:NotEmpty val name: String = ""
) {
    companion object {
        fun create(workspaceEntity: WorkspaceEntity): Workspace {
            return Workspace(workspaceEntity.name)
        }
    }
}
