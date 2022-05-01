package de.mcella.spring.learntool.user

import de.mcella.spring.learntool.security.Token
import de.mcella.spring.learntool.security.TokenProvider
import de.mcella.spring.learntool.user.dto.LoginRequest
import de.mcella.spring.learntool.user.dto.SignUpRequest
import de.mcella.spring.learntool.user.dto.User
import de.mcella.spring.learntool.user.exceptions.EmailAddressAlreadyInUseException
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.user.storage.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {

    fun authenticateUser(loginRequest: LoginRequest): Token {
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        loginRequest.email,
                        loginRequest.password
                )
        )
        SecurityContextHolder.getContext().authentication = authentication
        return Token(tokenProvider.createToken(authentication))
    }

    fun registerUser(signUpRequest: SignUpRequest): User {
        if (userRepository.existsByEmail(signUpRequest.email)) {
            throw EmailAddressAlreadyInUseException()
        }
        val password = passwordEncoder.encode(signUpRequest.password)
        val userEntity = UserEntity.create(signUpRequest, password)
        return User.create(userRepository.save(userEntity))
    }
}
