package de.mcella.spring.learntool.security.oauth2

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Service

const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
const val COOKIE_EXPIRE_SECONDS = 180

@Service
class HttpCookieOAuth2AuthorizationRequestRepository(val cookieUtils: CookieUtils) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = cookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        if (cookie != null) {
            return cookieUtils.deserialize(cookie, OAuth2AuthorizationRequest::class.java)
        }
        return null
    }

    override fun saveAuthorizationRequest(authorizationRequest: OAuth2AuthorizationRequest, request: HttpServletRequest, response: HttpServletResponse) {
        if (authorizationRequest == null) {
            cookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            cookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            return
        }

        val authorizationRequestCookie = cookieUtils.serialize(authorizationRequest)
        if (authorizationRequestCookie != null) {
            cookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequestCookie, COOKIE_EXPIRE_SECONDS)
        }
        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (redirectUriAfterLogin.isNotBlank()) {
            cookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS)
        }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request)
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        cookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        cookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
    }
}
