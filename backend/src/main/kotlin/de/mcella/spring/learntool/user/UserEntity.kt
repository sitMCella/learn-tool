package de.mcella.spring.learntool.user

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
)
