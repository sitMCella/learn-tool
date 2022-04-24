package de.mcella.spring.learntool.security.oauth2

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    val cookieUtils: CookieUtils
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse, exception: AuthenticationException) {
        val redirectUrl = cookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value ?: "/"
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("error", exception.localizedMessage)
                .build().toUriString()
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
