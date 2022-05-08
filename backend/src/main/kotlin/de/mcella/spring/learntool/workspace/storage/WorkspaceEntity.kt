package de.mcella.spring.learntool.workspace.storage

import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MAX_WORKSPACE_NAME_LENGTH
import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MIN_WORKSPACE_NAME_LENGTH
import de.mcella.spring.learntool.workspace.dto.Workspace
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Size

@Entity
@Table(name = "workspaces")
data class WorkspaceEntity(
    @Id @Size(min = MIN_WORKSPACE_NAME_LENGTH, max = MAX_WORKSPACE_NAME_LENGTH) val name: String = "",
    val userId: Long = 0L
) {
    companion object {
        fun create(workspace: Workspace): WorkspaceEntity {
            return WorkspaceEntity(workspace.name, workspace.userId.id)
        }

        fun hasUserId(workspaceEntity: WorkspaceEntity, userId: UserId): Boolean {
            return workspaceEntity.userId == userId.id
        }
    }
}
