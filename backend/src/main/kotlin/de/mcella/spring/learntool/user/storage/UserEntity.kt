package de.mcella.spring.learntool.user.storage

import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.dto.SignUpRequest
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
@Column(name = "id", nullable = false)
@GeneratedValue(strategy = GenerationType.IDENTITY)
val id: Long?,
    val name: String,
    val email: String,
    val imageUrl: String,
    val emailVerified: Boolean,
    val password: String,
    val authProvider: AuthProvider,
    val authProviderId: String
) {
    companion object {
        fun create(signUpRequest: SignUpRequest, password: String): UserEntity {
            return UserEntity(null, signUpRequest.name, signUpRequest.email, "", true, password, AuthProvider.local, "")
        }
    }
}
