package de.mcella.spring.learntool

import de.mcella.spring.learntool.security.UserPrincipal
import kotlin.collections.HashSet
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockOAuth2ScopeSecurityContextFactory : WithSecurityContextFactory<WithMockUser> {
    override fun createSecurityContext(mockOAuth2Scope: WithMockUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val scope: MutableSet<String> = HashSet()
        scope.add(mockOAuth2Scope.value)
        scope.add(mockOAuth2Scope.email)
        val request = OAuth2Request(null, mockOAuth2Scope.id, listOf(SimpleGrantedAuthority("ROLE_USER")), true, scope, null, null, null, null)
        val userPrincipal = UserPrincipal(1L, mockOAuth2Scope.email, mockOAuth2Scope.password, listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val auth: Authentication = OAuth2Authentication(request, TestingAuthenticationToken(userPrincipal, null, "ROLE_USER"))
        context.authentication = auth
        return context
    }
}
