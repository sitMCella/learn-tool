package de.mcella.spring.learntool.user.dto

import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.storage.UserEntity

data class User(
    val id: Long?,
    val name: String,
    val email: String,
    val imageUrl: String,
    val emailVerified: Boolean,
    val authProvider: AuthProvider,
    val authProviderId: String
) {
    companion object {
        fun create(userEntity: UserEntity): User {
            return User(userEntity.id, userEntity.name, userEntity.email, userEntity.imageUrl, userEntity.emailVerified, userEntity.authProvider, userEntity.authProviderId)
        }
    }
}
