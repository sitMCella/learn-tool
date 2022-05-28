package de.mcella.spring.learntool.workspace.storage

import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MAX_WORKSPACE_NAME_LENGTH
import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MIN_WORKSPACE_NAME_LENGTH
import de.mcella.spring.learntool.workspace.dto.Workspace
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

const val UUID_REGEXP = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

@Entity
@Table(name = "workspaces")
data class WorkspaceEntity(
    @Id @Pattern(regexp = UUID_REGEXP) val id: String = "",
    @Size(min = MIN_WORKSPACE_NAME_LENGTH, max = MAX_WORKSPACE_NAME_LENGTH) val name: String = "",
    val userId: Long = 0L
) {
    companion object {
        fun create(workspace: Workspace): WorkspaceEntity {
            return WorkspaceEntity(workspace.id, workspace.name, workspace.userId.id)
        }

        fun hasUserId(workspaceEntity: WorkspaceEntity, userId: UserId): Boolean {
            return workspaceEntity.userId == userId.id
        }
    }
}
