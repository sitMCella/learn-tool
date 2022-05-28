package de.mcella.spring.learntool.user.dto

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotExistentException
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity

data class UserId(
    val id: Long
) {
    companion object {
        fun create(userPrincipal: UserPrincipal): UserId {
            val userId = userPrincipal.id ?: throw UserNotExistentException()
            return UserId(userId)
        }

        fun create(userEntity: UserEntity): UserId {
            val userId = userEntity.id ?: throw UserNotExistentException()
            return UserId(userId)
        }

        fun create(workspaceEntity: WorkspaceEntity): UserId {
            return UserId(workspaceEntity.userId)
        }
    }
}
