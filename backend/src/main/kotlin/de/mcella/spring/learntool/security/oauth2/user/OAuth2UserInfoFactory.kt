package de.mcella.spring.learntool.security.oauth2.user

import de.mcella.spring.learntool.security.oauth2.user.exceptions.OAuth2AuthenticationProcessingException
import de.mcella.spring.learntool.user.AuthProvider
import org.springframework.stereotype.Service

@Service
class OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        if (registrationId.equals(AuthProvider.google.toString(), ignoreCase = true)) {
            return GoogleOAuth2UserInfo(attributes)
        }
        throw OAuth2AuthenticationProcessingException("Sorry, the login with $registrationId is not supported yet.")
    }
}
