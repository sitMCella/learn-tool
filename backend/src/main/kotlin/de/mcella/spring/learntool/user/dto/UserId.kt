package de.mcella.spring.learntool.user.dto

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity

data class UserId(
    val id: Long?
) {
    companion object {
        fun create(user: UserPrincipal): UserId {
            return UserId(user.id)
        }

        fun create(userEntity: UserEntity): UserId {
            return UserId(userEntity.id)
        }

        fun create(workspaceEntity: WorkspaceEntity): UserId {
            return UserId(workspaceEntity.userId)
        }
    }
}
