package de.mcella.spring.learntool.security

import de.mcella.spring.learntool.user.storage.UserEntity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

data class UserPrincipal(
    val id: Long?,
    @field:NotNull @field:NotEmpty val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any>
) : OAuth2User, UserDetails {
    companion object {
        fun create(userEntity: UserEntity): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            return UserPrincipal(userEntity.id, userEntity.email, userEntity.password, authorities, attributes = emptyMap())
        }

        fun create(userEntity: UserEntity, attributes: Map<String, Any>): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            return UserPrincipal(userEntity.id, userEntity.email, userEntity.password, authorities, attributes = attributes)
        }
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getName(): String {
        return id.toString()
    }

    override fun getAttributes(): Map<String, Any> {
        return attributes
    }
}
