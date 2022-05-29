package de.mcella.spring.learntool.workspace.dto

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Workspace(
    @field:NotNull @field:NotEmpty val id: String,
    @field:NotNull @field:NotEmpty val name: String,
    val userId: UserId
) {
    companion object {
        fun create(workspaceId: WorkspaceId, workspaceCreateRequest: WorkspaceCreateRequest, userPrincipal: UserPrincipal): Workspace {
            val userId = UserId.create(userPrincipal)
            return Workspace(workspaceId.id, workspaceCreateRequest.name, userId)
        }

        fun create(workspaceRequest: WorkspaceRequest, workspaceCreateRequest: WorkspaceCreateRequest, userPrincipal: UserPrincipal): Workspace {
            val userId = UserId.create(userPrincipal)
            return Workspace(workspaceRequest.id, workspaceCreateRequest.name, userId)
        }

        fun create(workspaceEntity: WorkspaceEntity): Workspace {
            val userId = UserId.create(workspaceEntity)
            return Workspace(workspaceEntity.id, workspaceEntity.name, userId)
        }
    }
}
