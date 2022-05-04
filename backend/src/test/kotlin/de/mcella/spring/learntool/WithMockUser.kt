package de.mcella.spring.learntool

import java.lang.annotation.Inherited
import org.springframework.security.test.context.support.WithSecurityContext

@Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@WithSecurityContext(factory = WithMockOAuth2ScopeSecurityContextFactory::class)
annotation class WithMockUser(val value: String = "user", val id: String = "123", val email: String = "test@google.com", val username: String = "", val roles: Array<String> = ["USER"], val password: String = "password")
