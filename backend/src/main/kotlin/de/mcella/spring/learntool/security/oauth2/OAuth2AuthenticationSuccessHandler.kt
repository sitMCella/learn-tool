package de.mcella.spring.learntool.security.oauth2

import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.BadRequestException
import de.mcella.spring.learntool.security.TokenProvider
import java.net.URI
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class OAuth2AuthenticationSuccessHandler(
    val tokenProvider: TokenProvider,
    val appProperties: AppProperties,
    val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    val cookieUtils: CookieUtils
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }
        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication): String {
        val redirectUri = cookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value ?: defaultTargetUrl

        if (!isAuthorizedRedirectUri(redirectUri)) {
            throw BadRequestException("We have got an Unauthorized Redirect URI and can't proceed with the authentication")
        }
        val token = tokenProvider.createToken(authentication)
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString()
    }

    protected fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        return appProperties.oauth2.authorizedRedirectUris
                .stream()
                .anyMatch { authorizedRedirectUri: String? ->
                    val authorizedURI: URI = URI.create(authorizedRedirectUri)
                    if (authorizedURI.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                            authorizedURI.port == clientRedirectUri.port) {
                        return@anyMatch true
                    }
                    false
                }
    }
}
