package de.mcella.spring.learntool.user

import de.mcella.spring.learntool.security.CurrentUser
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.User
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/me")
class UserController(private val authService: AuthService) {
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun getCurrentUser(@CurrentUser userPrincipal: UserPrincipal): User {
        return authService.getUser(userPrincipal)
    }
}
