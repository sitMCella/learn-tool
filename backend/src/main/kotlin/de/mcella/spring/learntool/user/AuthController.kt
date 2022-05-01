package de.mcella.spring.learntool.user

import de.mcella.spring.learntool.user.dto.AuthResponse
import de.mcella.spring.learntool.user.dto.LoginRequest
import de.mcella.spring.learntool.user.dto.RegistrationResponse
import de.mcella.spring.learntool.user.dto.SignUpRequest
import java.net.URI
import javax.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val token = authService.authenticateUser(loginRequest)
        val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
        return bodyBuilder.body(AuthResponse.create(token))
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    fun registerUser(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<RegistrationResponse> {
        val user = authService.registerUser(signUpRequest)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/user/me/${user.id}"))
        return bodyBuilder.body(RegistrationResponse(true, "User registered successfully"))
    }
}
