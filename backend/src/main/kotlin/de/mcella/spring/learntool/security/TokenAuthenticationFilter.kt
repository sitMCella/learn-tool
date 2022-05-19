package de.mcella.spring.learntool.security

import java.util.Optional
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Service
class TokenAuthenticationFilter(
    val tokenProvider: TokenProvider,
    val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val jwt = getJwtFromRequest(request)
            if (jwt.isPresent && StringUtils.hasText(jwt.get()) && tokenProvider.validateToken(jwt.get())) {
                val userId = tokenProvider.getUserIdFromToken(jwt.get())
                if (!userId.isNullOrBlank()) {
                    val userDetails = customUserDetailsService.loadUserById(userId.toLong())
                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): Optional<String> {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            Optional.of(bearerToken.substring(7, bearerToken.length))
        } else Optional.empty()
    }
}
