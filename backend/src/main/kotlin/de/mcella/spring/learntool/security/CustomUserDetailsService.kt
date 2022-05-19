package de.mcella.spring.learntool.security

import de.mcella.spring.learntool.common.toNullable
import de.mcella.spring.learntool.user.storage.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email).toNullable() ?: throw UsernameNotFoundException("User not found with email : $email")
        return UserPrincipal.create(user)
    }

    @Transactional
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id).toNullable() ?: throw UsernameNotFoundException("User not found with id : $id")
        return UserPrincipal.create(user)
    }
}
